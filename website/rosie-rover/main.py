import json
import logging
import os

from google.appengine.ext import ndb
from google.appengine.ext.webapp import template
import jinja2
import webapp2

from gcm import GCM
from models import Game, User
from models import Goal


API_KEY="AIzaSyBbCQFBuBJPSGgbEDengs11nHpXPqL1-zM" #my key
REGISTRATION_IDS = ["APA91bFHtHYtO8KwepJ6NU8iM_PHFMVqkHWow480bn7GmF-K3uRwvc9vKbG3iivY6io5nupN-3lFursoazty1jOu8EMy8iKM8p8qosd6VRO4rq1shOZ4SClYs-M3twHIpABeHQb6fgVwTkdaSHgo1PEU1PDawD-6B-04rqwjyhBBmje3QCLm1QC20MOQEsZP36oyi_z-Mj_6",
                    "APA91bGS2bBVfiTGscdkt6jX0Cin5c3FZIBc4o2uNiNGmfdQuNBbFAHsEl2Wxw2jIJgg1nQAb3DOVzJ61h6shQveyWXM7tKXBlpC7J9Bd7TLNV7s8QGtXqF7wqloIaycBq5OW73nr0CW1QFyEurUwoMY-x6f10P1O60wzgU082y5NcNlC2_H0s3xAtnAJLVQUciyL3FuKzoK"];


PARENT_KEY = ndb.Key("Entity", "user")

currentUser = "Pretend User";

jinja_env = jinja2.Environment(
  loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
  autoescape=True)

class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.redirect('/welcomePage')
        
class WecomePageHandler(webapp2.RequestHandler):
    def get(self):
        template = jinja_env.get_template("templates/welcomePage.html")
        self.response.out.write(template.render())
    
class RoverControlPageHandler(webapp2.RequestHandler):
    def get(self):
        template = jinja_env.get_template("templates/roverControlPage.html")
        self.response.out.write(template.render())
        
class CreateGamePageHandler(webapp2.RequestHandler):
    def get(self):
        global currentUser
        existingGame_query = Game.query(ancestor=PARENT_KEY).order(-Game.created_date_time).fetch()
        template = jinja_env.get_template("templates/createGamePage.html")
        self.response.out.write(template.render({"existingGame_query":existingGame_query, "username":currentUser}))

class CreateNewUserHandler(webapp2.RequestHandler):
    def post(self):
        # get the information the user entered
        username = self.request.get("username")
        password = self.request.get("password")
        
        # check that they entered things into both fields TODO
        
        # check the database to see if that user name is already being used TODO
        existingUser_query = User.query(User.username==username).fetch()
        if(existingUser_query):
            # TODO show a warning message that the username is already taken
            print "Already exists"
            self.redirect('/welcomePage')
        else:
            newUser = User(username = username,
                       password = password)
            newUser.put()
            global currentUser
            currentUser = newUser.username;
            self.redirect('/roverControlPage')
        
class UserLoginHandler(webapp2.RequestHandler):
    def post(self):
        username = self.request.get("username")
        password = self.request.get("password")
        # check the database to see if such a user exists
        existingUser_query = User.query(User.username==username).fetch()
        if(existingUser_query):
            # check if the password matches the user
            if(existingUser_query[0].password == password):
                global currentUser
                currentUser = existingUser_query[0].username
                self.redirect('/roverControlPage')
            else: 
                self.redirect('/welcomePage')
        else:
            self.redirect('/welcomePage') # TODO show a message for incorrect username/password combo
        
class UserLogoutHandler(webapp2.RequestHandler):
    def post(self):
        self.redirect('/welcomePage')
        # TODO allow the user to log out...

class DeleteUserHandler(webapp2.RequestHandler):
    def post(self):
        print "delete user"
        self.redirect('/welcomePage')

class CreateNewGameHandler(webapp2.RequestHandler):
    def post(self):       
        newGame = Game(parent = PARENT_KEY,
                       #urlSafeKeyGame =  TODO
                       gameName = self.request.get("gameTitle"),     
                       goals = [])
        newGame.put()
        self.redirect(self.request.referer)

class DeleteGameHandler(webapp2.RequestHandler):
    def post(self): 
        gameName = self.request.get("gameName")  
        existingGame_query = Game.query(Game.gameName==gameName).fetch()
        if (existingGame_query):
            existingGame_query[0].key.delete()
        self.redirect(self.request.referer)

class CreateNewGoalHandler(webapp2.RequestHandler):
    def post(self): 
        # save the goal
        currentGame = self.request.get("currentGame")   
        if(currentGame):     
            newGoal = Goal(game = currentGame,
            destination = self.request.get("destination"),     
               pointValue = int(self.request.get("pointValue")))
            newGoal.put()
            # attach the goal to the current game
            myGame = Game.query(Game.gameName==currentGame).fetch()[0]
            myGame.goals.append(newGoal)
            myGame.put()
        
        self.redirect(self.request.referer)

class DeleteGoalHandler(webapp2.RequestHandler):
    def post(self): 
        self.redirect(self.request.referer)

class HeartBeat(webapp2.RequestHandler):
    def post(self):
        gcm = GCM(API_KEY)
        statusCommand = self.request.get("status")
        print statusCommand;
        response = gcm.json_request(registration_ids=REGISTRATION_IDS, data={'data': statusCommand})
        self.response.out.write(json.dumps(response))

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/welcomePage',WecomePageHandler),
    ('/roverControlPage',RoverControlPageHandler),
    ('/createGamePage', CreateGamePageHandler),
    
    ('/createNewUser', CreateNewUserHandler),
    ('/userLogin', UserLoginHandler),
    ('/deleteUser', DeleteUserHandler),
    
    ('/createNewGame', CreateNewGameHandler),
    ('/deleteGame', DeleteGameHandler),
    
    ('/createNewGoal', CreateNewGoalHandler),
    ('/deleteGoal', DeleteGoalHandler),
    
    ('/heartBeat', HeartBeat)
], debug=True)
