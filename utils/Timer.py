import threading
from utils import Utils

timeInSeconds = 0
callback = None
timer = None


def set(ts, cb):
    global timeInSeconds, callback, timer
    if not Utils.isNumber(ts):
        print('Timer: time must be a number')
    if timer:
        print('Timer: Timer already set')
        return

    timeInSeconds = ts
    callback = cb
    timer = threading.Timer(0.0, countDown)


def add(ts):
    global timeInSeconds
    if not Utils.isNumber(ts):
        print('Timer: time must be a number')
    if not timer:
        print('Timer: No timer set')
        return
    timeInSeconds += ts


def substract(ts):
    global timeInSeconds
    if not Utils.isNumber(ts):
        print('Timer: time must be a number')
    if not timer:
        print('Timer: No timer set')
        return
    timeInSeconds -= ts


def start():
    if not timer:
        print('Timer: No timer set')
        return
    timer.start()


def stop():
    global timeInSeconds
    if not timer:
        print('Timer: No timer set')
        return
    timer.cancel()
    timeInSeconds = 0


def countDown():
    global timeInSeconds, callback, timer
    if timeInSeconds > 0:
        timeInSeconds -= 1
        mins, secs = divmod(timeInSeconds, 60)
        timeformat = '{:02d}:{:02d}'.format(mins, secs)
        print(timeformat, end='\r')
        timer = threading.Timer(1.0, countDown)
        timer.start()
    else:
        callback()


def reset():
    global timeInSeconds, callback, timer
    if timer:
        timer.cancel()
    timer = None
    timeInSeconds = 0
    callback = None
