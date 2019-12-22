def isNumber(x):
    try:
        float(x)
        return True
    except ValueError:
        return False


def wattToServoDutyCycle(watt):
    if isNumber(watt):
        watt = float(watt)
    else:
        return 2  # 750 watt
    if watt < 0:
        watt = 0
    if watt > 750:
        watt = 750
    # x     y    x   y
    # 750 = 2    0 = 11
    # -0.012 * x + 11
    print('Utils wattToServoDutyCycle: Watt ' + str(watt))
    dc = (-0.012 * watt) + 11
    print('Utils wattToServoDutyCycle: Duty cycle: ' + str(dc))
    return dc


def servoDutyCycleToWatt(dc):
    if isNumber(dc):
        dc = float(dc)
    else:
        return 750  # duty cycle 2
    if dc < 2:
        dc = 2
    if dc > 11:
        dc = 11
    # y     x    y   x
    # 750 = 2    0 = 11
    # -83.33 * x + 916.67
    print('Duty cycle servoDutyCycleToWatt Duty Cycle: ' + str(dc))
    watt = (-83.33 * dc) + 916.67
    print('Utils servoDutyCycleToWatt Watt:  ' + str(watt))
    return watt
