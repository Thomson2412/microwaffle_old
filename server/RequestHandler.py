import os
from http.server import SimpleHTTPRequestHandler
import Config
import urllib.parse as parse
from hardware import HardwareController
from utils import Timer, Utils
import json


class RequestHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=Config.webServerDir, **kwargs)

    def do_GET(self):
        print(self.path)
        commandCode = self.path.split('?')[0]

        if '/status' == commandCode:
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            status = {
                'busy': Timer.timeInSeconds > 0,
                'timer': Timer.timeInSeconds,
            }
            jsonRep = json.dumps(status)
            self.wfile.write(jsonRep.encode())

        elif '/watt' == commandCode:
            self.send_response(200)
            self.send_header('Content-length', '0')
            self.end_headers()
            query = parse.parse_qs(parse.urlparse(self.path).query)
            if 'watt' in query:
                watt = query['watt'][0]
                print('Watt set to: ' + watt)
                if not Utils.isNumber(watt):
                    print('RequestHandler: Position must be a number')
                    return
                position = Utils.wattToServoDutyCycle(watt)
                HardwareController.setServoPosition(position)

        elif '/start' == commandCode:
            self.send_response(200)
            self.send_header('Content-length', '0')
            self.end_headers()
            query = parse.parse_qs(parse.urlparse(self.path).query)
            if 'seconds' in query:
                timeInSeconds = int(query['seconds'][0])
                if timeInSeconds > 0:
                    Timer.set(timeInSeconds, self.timerEnded)
                    HardwareController.setRelay(1)
                    Timer.start()
                    print('RequestHandler: Start with time: ' + str(timeInSeconds))
                else:
                    print('RequestHandler: Time should be > 0: ' + str(timeInSeconds))

        elif '/stop' == commandCode:
            self.send_response(200)
            self.send_header('Content-length', '0')
            self.end_headers()
            Timer.stop()
            print('RequestHandler: Stop')
            self.timerEnded()

        elif self.path.find('.mp3') >= 0:
            pathTo = os.getcwd() + '/www' + self.path
            f = open(pathTo, 'rb')
            st = os.fstat(f.fileno())
            length = st.st_size
            data = f.read()

            self.send_response(200)
            self.send_header('Content-type', 'audio/mpeg')
            self.send_header('Content-Length', length)
            self.send_header('Accept-Ranges', 'bytes')
            self.end_headers()

            self.wfile.write(data)
            f.close()

        else:
            SimpleHTTPRequestHandler.do_GET(self)

        return

    def timerEnded(self):
        print('RequestHandler: Timer has ended or has stopped')
        HardwareController.setRelay(0)
        Timer.reset()
