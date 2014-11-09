from google.appengine.ext import ndb

class Goal(ndb.Model):
    game = ndb.StringProperty();
    destination = ndb.StringProperty();
    pointValue = ndb.IntegerProperty();
    
class User(ndb.Model):
    username = ndb.StringProperty()
    password = ndb.StringProperty()

class Game(ndb.Model):
    gameName = ndb.StringProperty()
    urlSafeKeyGame = ndb.StringProperty()
    goals = ndb.StructuredProperty(Goal, repeated=True)
    created_date_time = ndb.DateTimeProperty(auto_now_add=True)
    highScore = ndb.IntegerProperty(default=0);
    gameCreator = ndb.StructuredProperty(User)
