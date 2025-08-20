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


def main():
    line1 = "1 38977U 12061A   25217.87128340 -.00000134  00000+0  00000+0 0  9991"
    line2 = "2 38977   9.7957  52.9255 00053279 133.6689  65.4374  1.00272081 46441"
    ts = load.timescale()
    satellite = EarthSatellite(line1, line2, 'not needed', ts)
    specific_time = datetime(2025, 8, 6, 15, 43, 35, tzinfo=timezone.utc)
    t = ts.utc(specific_time.year, specific_time.month, specific_time.day, specific_time.hour, specific_time.minute, specific_time.second)
    gcrs = satellite.at(t)
    itrscoord = gcrs.frame_xyz(itrs)
    itrscoordInMeters = itrscoord.m
    print("time")
    print(t.utc_datetime())
    print("GCRS")
    print(gcrs.position.m)
    print("itrs")
    print(itrscoordInMeters)


if __name__ == "__main__":
    main()