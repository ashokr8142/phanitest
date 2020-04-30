import logging
import os

from flask import Flask
from flask import abort
from flask import jsonify
from flask import request
import requests

app = Flask(__name__)

@app.route('/StudyMetaData/getUserResources')
def get_user_resources():
  return "Request received.\n"


@app.route('/')
def health_check():
  return "Healthy\n"

if __name__ == '__main__':
  app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
