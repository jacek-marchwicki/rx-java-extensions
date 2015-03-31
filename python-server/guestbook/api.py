#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'


from guestbook.controller.api import GuestBookApi

import endpoints

app = endpoints.api_server([GuestBookApi,])