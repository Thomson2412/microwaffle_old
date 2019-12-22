from http.server import HTTPServer
from server.RequestHandler import RequestHandler
from hardware import HardwareController
import Config


def main():
    ws = None
    try:
        HardwareController.initGPIO()
        # HardwareController.initServo()
        HardwareController.initRelay()
        ws = initServer()
        ws.serve_forever()

    except KeyboardInterrupt:
        HardwareController.cleanUp()
        if ws:
            ws.server_close()


def initServer():
    return HTTPServer(('', Config.serverPort), RequestHandler)


if __name__ == '__main__':
    main()
