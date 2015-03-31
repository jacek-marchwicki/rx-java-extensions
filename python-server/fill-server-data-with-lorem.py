#!/usr/bin/env python

from apiclient.discovery import build
import loremipsum

guestbook = build('guestbook', 'v1', discoveryServiceUrl="https://atlantean-field-90117.appspot.com/_ah/api/discovery/v1/apis/{api}/{apiVersion}/rest")
dir(guestbook)
guestbook.listPosts().execute()
for i in range(0, 100):
  print "Filling: %d/%d" % (i, 100)
  name = "Lorem: %d" % i
  body = '\n\n'.join(i[2] for i in loremipsum.generate_paragraphs(3))
  guestbook.createPost(body={"body":body, "name":name}).execute()
  pass

