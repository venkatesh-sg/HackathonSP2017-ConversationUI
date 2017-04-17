# UMKC-HackathonSP2017-ConversationUI
  _This is an Use Case Provided by Yoodle at Spring 2017 UMKC and H&R Block Hackathon held at UMKC. We need to design an application with which the user should be able to communicate and book an appointment with the doctor for the problem he is having._

The Presentation slides can be accessed from here 
https://github.com/nandanamudi/UMKC-HackathonSP2017-ConversationUI/blob/master/Matrix%20Conversation%20UI.pdf

## Architecture of this Project

![save](https://github.com/venkatesh-sg/UMKC-HackathonSP2017-ConversationUI/blob/master/Documentation/save.png)

## Description: 

**we desgined the application in three stages**
1. _Taking the users input and feeding it to the service as Json_
2. _Feteching this input and carrying the related data toward the Spark Service where NLP processing is done and feature vectors are generated and is given to Trained Naive Bayes Model._
3. _Naive Bayes Model Predicts the Specialization needed to attend the problem provided by the user. The result specilization class is fed to the service again. Here the Service pull the doctors from the specialization and asks for the user's choice to select the Doctor based on the distance or the rating of that particular Doctor: the details we already had in the database._


**[Here is the video](https://www.youtube.com/watch?v=BVR6R2jYO_g) to our project which explains the description said above with a small demo of our project.**

