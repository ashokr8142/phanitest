# Use the official lightweight Python image.
# https://hub.docker.com/_/python
FROM python:3.7-slim

# Copy local code to the container image.
ENV APP_HOME /app
WORKDIR $APP_HOME
COPY . ./app

# Install production dependencies.
RUN pip install -r app/requirements.txt

# Run the script.
CMD ["python3", "app/subscription_main.py"]
