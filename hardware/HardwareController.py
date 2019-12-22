import RPi.GPIO as GPIO
import time
from utils import Utils
import Config

isRelayInit = 0
isServoInit = 0
pwm = None
currentServoDutyCycle = 0
currentRelayValue = 0


def initGPIO():
    GPIO.setmode(GPIO.BCM)


def initRelay():
    global isRelayInit
    if Config.relayPin < 0:
        print('HardwareController: invalid pin')
        return
    GPIO.setup(Config.relayPin, GPIO.OUT)
    GPIO.output(Config.relayPin, 1)
    isRelayInit = 1


def initServo():
    global isServoInit
    if Config.servoPin < 0:
        print('HardwareController: invalid pin')
        return
    global pwm
    GPIO.setup(Config.servoPin, GPIO.OUT)
    pwm = GPIO.PWM(Config.servoPin, 50)
    pwm.start(0)
    setServoPosition(Utils.wattToServoDutyCycle(750))
    isServoInit = 1


def setRelay(value):
    global currentRelayValue
    currentRelayValue = value
    GPIO.output(Config.relayPin, (-1 + value) * -1)


def setServoPosition(dutyCycle):
    global currentServoDutyCycle
    if not isServoInit:
        print('HardwareController: Init servo first')
        return
    if not pwm:
        print('HardwareController: Init pwm first')
        return
    if not Utils.isNumber(dutyCycle):
        print('HardwareController: Position must be a number')
        return

    if dutyCycle < 2:
        dutyCycle = 2
    if dutyCycle > 11:
        dutyCycle = 11

    pwm.ChangeDutyCycle(dutyCycle)
    currentServoDutyCycle = dutyCycle
    time.sleep(0.6)
    pwm.ChangeDutyCycle(0)


def cleanUp():
    global isRelayInit, isServoInit
    if pwm:
        pwm.stop()
    GPIO.cleanup()
    isRelayInit = 0
    isServoInit = 0
