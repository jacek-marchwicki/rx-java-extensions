#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'

from google.appengine.ext import ndb


class Post(ndb.Model):
    name = ndb.StringProperty(required=True)
    body = ndb.TextProperty(required=True)