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
    goals = ndb.StructuredProperty(Goal, repeated=True)
    created_date_time = ndb.DateTimeProperty(auto_now_add=True)
    highScore = ndb.IntegerProperty(default=0);
    gameCreator = ndb.StringProperty()

class Admin(ndb.Model):
    email = ndb.StringProperty()
    
class ComData(ndb.Model):
    robotBatteryLife = ndb.FloatProperty();
    phoneBatteryLife = ndb.FloatProperty();
    GPSx = ndb.FloatProperty();
    GPSy = ndb.FloatProperty();
    displayVideo = ndb.FloatProperty();
    
    warning = ndb.IntegerProperty();
    emergency = ndb.IntegerProperty();
    fire = ndb.IntegerProperty();
    ignoreus = ndb.IntegerProperty();
    leftMotor = ndb.IntegerProperty();
    rightMotor = ndb.IntegerProperty();
    pan = ndb.IntegerProperty();
    tilt = ndb.IntegerProperty();
    
class UserAuthDateTime(ndb.Model):
    userName = ndb.StringProperty();
    startDateTime = ndb.DateTimeProperty();
    endDateTime = ndb.DateTimeProperty();