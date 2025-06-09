from concurrent.futures import ProcessPoolExecutor
import math
import os
from sgp4.api import Satrec
import json
from datetime import datetime, timedelta, timezone
from skyfield.api import load, EarthSatellite
from skyfield.framelib import itrs
from astropy import units as u
from io import StringIO
import psycopg2
import time
import sys

# Interval
interval_minute = None

# Step size
step_seconds = None

# Minute size in seconds
SECONDS = 60

def main():
    global interval_minute
    interval_minute = int(sys.argv[1])

    global step_seconds
    step_seconds = int(sys.argv[2])
    
    # Used for benchmarking how fast calculations take
    start = time.time()

    # Want to obtain coordinates for 15 minute intervals
    currtime = datetime.now(timezone.utc)
    start_time = round_to_the_Interval_minute(currtime)

    # Convert tledata from .txt file to list so multithreading can be done
    tle_list = convert_to_list()

    # add time to the list of coordinates
    args_list = [(norad_id, name, line1, line2, start_time, interval_minute, step_seconds) for (norad_id, name, line1, line2) in tle_list]

    # Begin calculating coordinates and storing them in a results list
    with ProcessPoolExecutor(max_workers=5) as executor:
        results = list(executor.map(calculate_coords_itrs, args_list))
    
    # # Place the coordinates into a file
    end = time.time()
    elapsed = end - start
    print("Time it took "+ str(elapsed) + " seconds\n")
    insert_to_database(results, start_time)

    # with open("coordinates.txt", "r") as f:
    #     print(sum(1 for _ in f))

def insert_to_database(results, start_time):
    # Connect to the DB
    conn = psycopg2.connect(
        dbname="satellara_db",
        user=os.getenv("DB_USERNAME"),        
        password=os.getenv("DB_PASSWORD"),
        host="localhost",
        port="5332"
    )

    # Create cursor for database inserts
    cursor = conn.cursor()
    
    # Create a StringIO object which will be used to create a temp file for the satellite coordinates
    buffer = StringIO()

    # Write all the points to the buffer
    for satellite_points in results:
        for coordinate in satellite_points:
            buffer.write(f"{coordinate['norad_id']}\t{coordinate['name']}\t{coordinate['time']}\t{coordinate['x']}\t{coordinate['y']}\t{coordinate['z']}\n")
    
    # Start at the beginning of the buffer
    buffer.seek(0)

    # Insert coordinate points in the database at correct partition
    cursor.copy_from(buffer, "satellite_location_" + start_time.strftime("%Y_%m%d_%H%M"), columns=('norad_id', 'name', 'time', 'x', 'y', 'z'))
    conn.commit()

    # Close the connection once finished
    conn.close()


def calculate_coords_itrs(args):
    # Extract data from argument list
    norad_id, name, line1, line2, start_time, interval_minute, step_seconds = args

    # list of coordinates for an individual satellite
    coordinates_list = []
    
    
    try:
        # needed variable for satellite coordinate calculation
        ts = load.timescale()

        # Create Earth Satellite Object 
        # 'not need' is supposed to be where name of satelltie goes, but this is not necessary for calculations
        satellite = EarthSatellite(line1, line2, 'not needed', ts)

        # Calculating coordinate loop begins
        for i in range(0, interval_minute * SECONDS, step_seconds):
            # Obtain time at a specifc offset of 5 seconds
            specific_time = start_time + timedelta(seconds=i)

            # Converting to utc because utc is the standard for coordinate calculation
            t = ts.utc(specific_time.year, specific_time.month, specific_time.day, specific_time.hour, specific_time.minute, specific_time.second)
            
            # .at(time) method calculates coordinates in Geocentric Celestial Reference System
            gcrs = satellite.at(t)

            # Convert coordinates to International Terrestrial Reference System (ITRS)
            # ITRS coordinates are plottable in CesiumJS
            itrscoord = gcrs.frame_xyz(itrs)

            # Coordinates are orinally calculated in AU and need to be converted to meters for cesium
            itrscoordInMeters = itrscoord.m

            # Format data for json
            satellitedata = {
                "norad_id" : norad_id,
                "name" : name,
                "time" : t.utc_iso(),
                "x" : itrscoordInMeters[0],
                "y" : itrscoordInMeters[1],
                "z" : itrscoordInMeters[2]
            }

            # add calculated coordinate to coordinate list if not decayed
            satelliteDecayed = math.isnan(itrscoordInMeters[0]) or math.isnan(itrscoordInMeters[1]) or math.isnan(itrscoordInMeters[2])
            if not satelliteDecayed:
                coordinates_list.append(satellitedata)
        return coordinates_list
    except Exception as e:
        return json.dumps({
        "norad_id": norad_id,
        "error": str(e)
    })

def round_to_the_Interval_minute(time: datetime):
    global interval_minute
    time = time.replace(second=0, microsecond=0)
    floored_minute = (time.minute // interval_minute) * interval_minute
    floored_time = time.replace(minute=floored_minute)
    next_cycle = floored_time + timedelta(minutes=interval_minute)
    return next_cycle


def convert_to_list():
    tle_list = []
    with open("./scripts/tledata.txt", "r") as datafile:
        while True:
            name = datafile.readline().strip() 
            if not name:
                #EOF
                break
            
            line1 = datafile.readline()
            line2 = datafile.readline()

            if not line1 or not line2:
                #Something is wrong with the data
                break
            norad_id = int(line2.split()[1])
            tle_list.append((norad_id, name, line1, line2))
    return tle_list

if __name__ == "__main__":
    main()
