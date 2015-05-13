import json
import os

from datetime import datetime
from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext.webapp import template
import jinja2
import webapp2

from models import Game, User, Goal, Admin, ComData, UserAuthDateTime


API_KEY="AIzaSyBbCQFBuBJPSGgbEDengs11nHpXPqL1-zM" #my key
REGISTRATION_IDS = ["APA91bEcCK7NiTH2DQ9Nb8xsq1OYXnusK_2pm2W7lxdfJdg0LpW_FF3I8JL851EPgyoClnJx3Xju-owsk20ccmXRXWAsVfX9sqQlktK2k88Egk5MFx3ry57Uw0Hwzn_FS1E6PD_j3tOKWhMRJH288zeV6suu1TOVl9Eq5IDEVlEDtDoR7RP4qsscgY7UQ53dHtoHXw71fyjq",
                    "APA91bGNwoXCrhpG-p5WAYLi556-sA9b1dQrZbt-D0nrL3udC-cc4qoscebLml4cMYPs4Zo-qDVCCrFVlz9489RXr810udozD9lC3Zdw3BuS66o9QEtMOJ8NPhd5PUaMP_H4VOsUIYGFFUq7NWh4AUgM783Ka3b8e6Hh2bqS_BFYiwj58QRpaQD0Lxw8VULzvh62fKCDTOhn"]


PARENT_KEY = ndb.Key("Entity", "user")

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
        existingGame_query = Game.query(ancestor=PARENT_KEY).order(-Game.created_date_time).fetch()
        template = jinja_env.get_template("templates/roverControlPage.html")
        self.response.out.write(template.render({"existingGame_query":existingGame_query}))
        
class CreateGamePageHandler(webapp2.RequestHandler):
    def get(self):
        template = jinja_env.get_template("templates/createGamePage.html")
        self.response.out.write(template.render())

class AdminPageHandler(webapp2.RequestHandler):
    def get(self):
        if users.get_current_user():    
            #url = users.create_logout_url(self.request.uri)
            emailAddress = users.get_current_user().email()
            
            admins_Query = Admin.query(ancestor=PARENT_KEY).fetch()
            alreadyAdmin = False
            
            for admin in admins_Query:
                if admin.email.encode('ascii','ignore') == emailAddress:
                    alreadyAdmin = True

            if (alreadyAdmin):
                # if an approved user is logged in:
                games_query = Game.query(ancestor=PARENT_KEY).fetch()
                users_query = User.query(ancestor=PARENT_KEY).fetch()
            
                logoutURL = users.create_logout_url(self.request.uri)
            
                template = jinja_env.get_template("templates/superSecretAdminPage.html")
                self.response.out.write(template.render({"logoutURL":logoutURL, "emailAddress":emailAddress, "games":games_query, "users":users_query, "admins":admins_Query}))
            else:
                # they are not an approved admin
                logoutURL = users.create_logout_url(self.request.uri)
                template = jinja_env.get_template("templates/adminSignInPage.html")
                self.response.out.write(template.render({"loginURL":logoutURL, "buttonText":"Logout"}))
        else:
            # there isn't anyone logged in
            loginURL = users.create_login_url(self.request.uri)
            template = jinja_env.get_template("templates/adminSignInPage.html")
            self.response.out.write(template.render({"loginURL":loginURL,"buttonText":"Login"}))

class AdminSchedulerPageHandler(webapp2.RequestHandler):
    def get(self):
        template = jinja_env.get_template("templates/welcomePage.html")
        self.response.out.write(template.render())

class CreateNewAdminHandler(webapp2.RequestHandler):
    def post(self): 
        # save the admin        
        newAdminEmail = self.request.get("email")
        newAdmin = Admin(parent = PARENT_KEY,
                       email = newAdminEmail)     
        newAdmin.put()
        self.redirect(self.request.referer)

class CreateNewUserHandler(webapp2.RequestHandler):
    def post(self):
        # get the information the user entered
        username = self.request.get("username")
        password = self.request.get("password")
        
        # check the database to see if that user name is already being used
        existingUser_query = User.query(User.username==username).fetch()
        if(existingUser_query):
            userAlreadyExists = 1
        else:
            userAlreadyExists = 0
            # put the user in the data base
            newUser = User(
                           parent = PARENT_KEY,
                           username = username,
                       password = password)
            newUser.put()
        
        # return the information to the js page that made the ajax request 
        response = {"alreadyExists" : userAlreadyExists}   
        self.response.out.write(json.dumps(response))
        
class UserLoginHandler(webapp2.RequestHandler):
    def post(self):
        # get the information the user entered
        username = self.request.get("username")
        password = self.request.get("password")
        
        # check the database to see if such a user exists
        existingUser_query = User.query(User.username==username).fetch()
        if(existingUser_query):
            # check if the password matches the user
            if(existingUser_query[0].password == password):
                correctPassword = 1
            else: 
                correctPassword = 0
        else:
            correctPassword = 0
            
        # return the information to the js page that made the ajax request 
        response = {"correctPassword" : correctPassword}   
        self.response.out.write(json.dumps(response))
 
class DeleteUserHandler(webapp2.RequestHandler):
    def post(self):
        # get the information the user entered
        username = self.request.get("username")
        
        # search the database for the user
        existingUser_query = User.query(User.username==username).fetch()
        if(existingUser_query): # make sure we found the user
            # remove said user from the database
            existingUser_query[0].key.delete()
        self.redirect(self.request.referer)    

class AddUserAuthTimeHandler(webapp2.RequestHandler):
    def post(self):
        # get the information the admin entered
        startDateTime = self.request.get("startDateTime")
        endDateTime = self.request.get("endDateTime")
        userName = self.request.get("userName")
        
        # The times come in as unicode objects. Convert to strings and then to datetime objects.
        startDateTime = datetime.strptime(str(startDateTime), '%Y-%m-%dT%H:%M') 
        endDateTime = datetime.strptime(str(endDateTime), '%Y-%m-%dT%H:%M') 
        
        # Create the userAuthTime object and put it in the datastore.
        newUserAuthTime = UserAuthDateTime(parent = PARENT_KEY,
                       startDateTime = startDateTime,     
                       endDateTime = endDateTime,
                       userName = userName)
        newUserAuthTime.put()
        
        self.redirect(self.request.referer)  

class CheckUserAuthStatusHandler(webapp2.RequestHandler):
    def post(self):   
        # get the information from the page
        userName = self.request.get("userName")
        # get the current date
        currentDate = datetime.today() 
                     
        # query the datebase to learn if there are any dates that this user is authorized for this time
        userAuthDateTime_query = UserAuthDateTime.query(UserAuthDateTime.userName==userName).fetch()
        
        # check if the user is authorized
        response = {"userAuthStatus" : "false"}
        if (userAuthDateTime_query):
            for userAuthDateTime in userAuthDateTime_query:
                if (currentDate > userAuthDateTime.startDateTime and currentDate < userAuthDateTime.endDateTime):
                    response = {"userAuthStatus" : "true"}
          
        self.response.out.write(json.dumps(response))
        
class CreateNewGameHandler(webapp2.RequestHandler):
    def post(self): 
        gameName = self.request.get("gameTitle") 
        creator = self.request.get("user")
        newGame = Game(parent = PARENT_KEY,
                       gameName = gameName,     
                       goals = [],
                       gameCreator = creator)
        newGame.put()
        self.redirect(self.request.referer)

class DeleteGameHandler(webapp2.RequestHandler):
    def post(self): 
        gameName = self.request.get("gameName")  
        
        # search the database for the game
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
        gameName = self.request.get("deleteGoalCurrentGame")
        destination = self.request.get("deleteGoalDestination")
        existingGame_query = Game.query(Game.gameName==gameName).fetch()
        if (existingGame_query):
            for goal in existingGame_query[0].goals:
                if(goal.destination == destination):
                    existingGame_query[0].goals.remove(goal)
                    existingGame_query[0].put()
                    break
        
        self.redirect(self.request.referer)

class GetUserGamesHandler(webapp2.RequestHandler):
    def post(self):
        user = self.request.get("user")
        gameQuery = Game.query(Game.gameCreator==user).fetch()
        gameArray = []
        for game in gameQuery:
            gameArray.append(game.gameName)
        response = {"games" : gameArray}  
        self.response.out.write(json.dumps(response))

class GetGoalsHandler(webapp2.RequestHandler):
    def post(self):
        user = self.request.get("user")
        gamename = self.request.get("game")
        gameQuery = Game.query(Game.gameCreator==user, Game.gameName == gamename).fetch()
        destinationArray = []
        pointsArray = []
        for goal in gameQuery[0].goals:
            destinationArray.append(goal.destination)
            pointsArray.append(goal.pointValue)
        response = {"destinations" : destinationArray,"points":pointsArray}  
        self.response.out.write(json.dumps(response))

class ComsHandler(webapp2.RequestHandler):
    def post(self):
         
        # get the information
        robotBatteryLife = float(self.request.get("robotBatteryLife"))
        phoneBatteryLife = float(self.request.get("phoneBatteryLife"))
        GPSx = float(self.request.get("GPSx"))
        GPSy = float(self.request.get("GPSy"))
        displayVideo = float(self.request.get("displayVideo"))
        
        comData = ComData.query().fetch()
        
        if comData: # it already exists, so simply update it
            comData = comData[0]
            comData.robotBatteryLife = robotBatteryLife
            comData.phoneBatteryLife = phoneBatteryLife
            comData.GPSx = GPSx
            comData.GPSy = GPSy
            comData.displayVideo = displayVideo
        else: # we need to create the data structure
            comData = ComData(robotBatteryLife = robotBatteryLife,
                              phoneBatteryLife = phoneBatteryLife,
                              GPSx = GPSx,
                              GPSy = GPSy,
                              warning = False,
                              emergency = False,
                              fire = False,
                              ignoreus = False,
                              leftMotor = 0,
                              rightMotor = 0,
                              pan = 0,
                              tilt = 0,
                              displayVideo = 0)        
    
        # return information 
        response = {"warning" : comData.warning,
                    "emergency": comData.emergency,
                    "fire": comData.fire,
                    "ignoreus": comData.ignoreus,
                    "leftMotor": comData.leftMotor,
                    "rightMotor": comData.rightMotor,
                    "pan":comData.pan,
                    "tilt":comData.tilt} 
        
        comData.pan = 0 # we want to set these to 0 so we know that they have performed the requested movement
        comData.tilt = 0
        comData.fire = False
        comData.put()
          
        self.response.out.write(json.dumps(response))

class UpdateComVarsHandler(webapp2.RequestHandler):
    def post(self):
        warning = int(self.request.get("warning"))
        emergency = int(self.request.get("emergency"))
        fire = int(self.request.get("fire"))
        ignoreus = int(self.request.get("ignoreus"))
        leftMotor = int(self.request.get("leftMotor"))
        rightMotor = int(self.request.get("rightMotor"))
        pan = int(self.request.get("pan"))
        tilt = int(self.request.get("tilt"))
        
        comData = ComData.query().fetch()
        
        if comData: # it already exists, so simply update it
            comData = comData[0]
            comData.warning = warning
            comData.emergency = emergency
            comData.fire = comData.fire or fire
            comData.ignoreus = ignoreus
            comData.leftMotor = leftMotor
            comData.rightMotor = rightMotor
            if pan != 0: # we don't want to change them if they aren't 0 since that means the phone has not requested the values yet
                comData.pan = pan
            if tilt != 0:
                comData.tilt = tilt
            if fire:
                comData.fire = fire
            
        else: # we need to create the data structure
            comData = ComData(robotBatteryLife = 0,
                              phoneBatteryLife = 0,
                              GPSx = -1,
                              GPSy = -1,
                              warning = warning,
                              emergency = emergency,
                              fire = fire,
                              ignoreus = ignoreus,
                              leftMotor = leftMotor,
                              rightMotor = rightMotor,
                              pan = 0,
                              tilt = 0,
                              displayVideo = 0)
        
        comData.put()
        
        # return information 
        response = {"robotBatteryLife" : comData.robotBatteryLife,
                    "phoneBatteryLife": comData.phoneBatteryLife,
                    "GPSx": comData.GPSx,
                    "GPSy": comData.GPSy}
        self.response.out.write(json.dumps(response))

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/welcomePage',WecomePageHandler),
    ('/roverControlPage',RoverControlPageHandler),
    ('/createGamePage', CreateGamePageHandler),
    
    ('/superSecretAdminPage', AdminPageHandler),
    ('/superSecretAdminSchedulerPage', AdminSchedulerPageHandler),
    
    ('/createNewAmin', CreateNewAdminHandler),
    ('/createNewUser', CreateNewUserHandler),
    ('/userLogin', UserLoginHandler),
    ('/deleteUser', DeleteUserHandler),
    ('/addUserAuthTime', AddUserAuthTimeHandler),
    ('/checkUserAuthStatus', CheckUserAuthStatusHandler),
    
    ('/createNewGame', CreateNewGameHandler),
    ('/deleteGame', DeleteGameHandler),
    
    ('/createNewGoal', CreateNewGoalHandler),
    ('/deleteGoal', DeleteGoalHandler),
    
    ('/getUserGames', GetUserGamesHandler),
    ('/getGoals', GetGoalsHandler),
    
    ('/coms', ComsHandler),
    ('/updateComVars', UpdateComVarsHandler)
], debug=True)
    