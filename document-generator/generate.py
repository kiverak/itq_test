import configparser
import requests
import random
import string
import sys
from pathlib import Path


def random_string(length=10):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))


def load_config():
    config_path = Path(__file__).parent / "config.ini"
    if not config_path.exists():
        print("Config file config.ini not found")
        sys.exit(1)

    config = configparser.ConfigParser()
    config.read(config_path)
    return config


def create_document(api_url, index):
    payload = {
        "author": f"Author_{index}",
        "title": f"Document_{index}",
        "content": random_string(50)
    }

    try:
        response = requests.post(api_url, json=payload, timeout=5)
        if response.status_code == 200 or response.status_code == 201:
            print(f"[OK] Created document {index}")
        else:
            print(f"[ERR] Failed to create document {index}: {response.status_code} {response.text}")
    except Exception as e:
        print(f"[ERR] Exception for document {index}: {e}")


def main():
    config = load_config()

    count = int(config["generator"]["count"])
    api_url = config["generator"]["api_url"]

    print(f"Generating {count} documents via API: {api_url}")

    for i in range(count):
        create_document(api_url, i)

    print("Done.")


if __name__ == "__main__":
    main()
