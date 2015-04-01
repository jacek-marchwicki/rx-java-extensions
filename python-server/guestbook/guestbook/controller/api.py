#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

MAX_LIMIT = 1000

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'

from google.appengine.ext import ndb
from google.appengine.ext.ndb import Cursor

from guestbook.messages.post import *
from guestbook.model.post import Post
from google.appengine.api import memcache

from protorpc import remote
import endpoints
from protorpc import message_types
from config import AUDIENCES, SCOPES, CLIENT_IDS

DESCRIPTION = """
Guestbook API
"""


@endpoints.api(name='guestbook',
               version='v1',
               allowed_client_ids=CLIENT_IDS,
               audiences=AUDIENCES,
               auth_level=endpoints.AUTH_LEVEL.NONE,
               description=DESCRIPTION,
               owner_name="Jacek Marchwicki <jacek.marchwicki@gmail.com>",
               package_path="com/guestbook",
               title="Guestbook API",
               scopes=SCOPES)
class GuestBookApi(remote.Service):
    """Cards API v1."""

    @staticmethod
    def _post_from_db(post):
        return PostMessage(id=post.key.urlsafe(),
                           name=post.name,
                           body=post.body)

    @staticmethod
    def _post_id_from_db(key):
        return PostIdMessage(id=key.urlsafe())

    @staticmethod
    def _get_post_or_raise(post_id):
        try:
            key = ndb.Key(urlsafe=post_id)
            if key.kind() != "Post":
                raise endpoints.NotFoundException('Post "%s" not found.' % (post_id,))
            card = key.get()
        except TypeError:
            raise endpoints.NotFoundException('Post "%s" not found.' % (post_id,))
        if not card:
            raise endpoints.NotFoundException('Post "%s" not found.' % (post_id,))
        return card


    @endpoints.method(PostsRequest, PostsCollection,
                      path='posts', http_method='GET',
                      name='listPosts')
    def posts_list(self, request):
        """
        List posts
        """
        limit = min(request.limit, MAX_LIMIT)
        start_cursor = Cursor(urlsafe=request.next_token)

        query = Post.query()
        posts, next_cursor, more = query.fetch_page(limit, start_cursor=start_cursor, batch_size=limit)

        next_token = next_cursor.urlsafe() if next_cursor and more else None
        resp_posts = [self._post_from_db(post) for post in posts]

        return PostsCollection(posts=resp_posts, next_token=next_token)

    @endpoints.method(PostsRequest, PostsIdsCollection,
                      path='posts_ids', http_method='GET',
                      name='listPostsIds')
    def posts_ids_list(self, request):
        """
        List posts
        """
        limit = min(request.limit, MAX_LIMIT)

        start_cursor = Cursor(urlsafe=request.next_token)

        query = Post.query()
        posts, next_cursor, more = query.fetch_page(limit, start_cursor=start_cursor, batch_size=limit, keys_only=True)

        next_token = next_cursor.urlsafe() if next_cursor and more else None
        resp_posts = [self._post_id_from_db(post) for post in posts]

        collection = PostsIdsCollection(posts=resp_posts, next_token=next_token)
        return collection

    @endpoints.method(ID_RESOURCE, PostMessage,
                      path='posts/{id}', http_method='GET',
                      name='getPost')
    def post_get(self, request):
        """
        Get post via id
        """
        card = self._get_post_or_raise(request.id)
        return self._post_from_db(card)

    @endpoints.method(CreatePostMessage, PostMessage,
                      path='posts', http_method='POST',
                      name='createPost')
    def post_create(self, request):
        """
        Create new post
        """
        post = Post(name=request.name,
                    body=request.body)
        post.put()
        return self._post_from_db(post)

    @endpoints.method(UPDATE_POST_MESSAGE, PostMessage,
                      path='posts/{id}', http_method='PATCH',
                      name='updatePost')
    def post_update(self, request):
        """
        Update post. Fields are not required
        """
        post = self._get_post_or_raise(request.id)
        if request.name:
            post.name = request.name
        if request.body:
            post.body = request.body
        post.put()
        return self._post_from_db(post)

    @endpoints.method(ID_RESOURCE, message_types.VoidMessage,
                      path='posts/{id}', http_method='DELETE',
                      name='removePost')
    def post_remove(self, request):
        """
        Remove post
        """
        card = self._get_post_or_raise(request.id)
        card.key.delete()
        return message_types.VoidMessage()