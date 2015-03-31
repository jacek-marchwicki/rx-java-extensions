# Python guestbook server

## Prepare

install python from brew

```bash
brew install python
```

install google cloud

```bash
curl https://sdk.cloud.google.com | bash
```

install google cloud gae extension
```bash
gcloud components update gae-python
```

create virtualenv in `python-server` drectory :

```bash
virtualenv --python /usr/local/Cellar/python/2.7.8_1/bin/python2.7 --no-site-packages venv
```

run virutalenv:

```bash
. ./venv/bin/activate
```

install requrements:

```bash
cd guestbook
pip install --upgrade -r requirements.txt
```

setup google app engine environment

```bash
cd guestbook
gaenv
```

## Run

run server:
```bash
cd guestbook
dev_appserver.py ./
```

## Browse application

* [Application[(http://localhost:8080)
* [Api browser](http://localhost:8000)
* [Api Explorer](http://localhost:8080/_ah/api/explorer)

## Deploy

```bash
appcfg.py --oauth2 update guestbook/
```

* [Application](https://atlantean-field-90117.appspot.com/)
* [Api Explorer](https://atlantean-field-90117.appspot.com/_ah/api/explorer)

## Usage from python

```python
from apiclient.discovery import build
guestbook = build('guestbook', 'v1', discoveryServiceUrl="https://atlantean-field-90117.appspot.com/_ah/api/discovery/v1/apis/{api}/{apiVersion}/rest")
dir(guestbook)
guestbook.listPosts().execute()
```

  

