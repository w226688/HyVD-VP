import socket

def send_jar(jar_path, host, port=9025):
    with open(jar_path, 'rb') as f:
        data = f.read()
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    sock.sendall(data)
    sock.close()
    print(f"Sent {len(data)} bytes to {host}:{port}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) > 2:
        host = sys.argv[2]
        port = int(sys.argv[3]) if len(sys.argv) > 3 else 9025
        send_jar(sys.argv[1], host, port)
    else:
        print("Usage: python payload_sender.py <file> <host> [port]")
        print("Examples:")
        print("  python payload_sender.py helloworld.jar 192.168.1.100")
        print("  python payload_sender.py payload.bin 192.168.1.100 9020")
