#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'


from protorpc import messages, message_types
import endpoints

ID_RESOURCE = endpoints.ResourceContainer(
    message_types.VoidMessage,
    id=messages.StringField(1, required=True))


class UpdatePostMessage(messages.Message):
    name = messages.StringField(2, required=False)
    body = messages.StringField(3, required=False)

UPDATE_POST_MESSAGE = endpoints.ResourceContainer(
    UpdatePostMessage,
    id=messages.StringField(1, required=True))


class CreatePostMessage(messages.Message):
    name = messages.StringField(1, required=True)
    body = messages.StringField(2, required=True)


class PostsRequest(messages.Message):
    next_token = messages.StringField(1, required=False, default=None)
    limit = messages.IntegerField(2, required=False, default=10)


class PostMessage(messages.Message):
    id = messages.StringField(1, required=True)
    name = messages.StringField(2, required=True)
    body = messages.StringField(3, required=True)


class PostsCollection(messages.Message):
    posts = messages.MessageField(PostMessage, 1, repeated=True)
    next_token = messages.StringField(2)