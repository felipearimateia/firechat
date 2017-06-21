const functions = require('firebase-functions');
const admin = require('firebase-admin');
const gcs = require('@google-cloud/storage')({keyFilename: 'service-account.json'});
const config = functions.config();

admin.initializeApp(config.firebase);
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.newUser = functions.auth.user().onCreate(event => {
  const user = event.data;

  const email = user.email;
  const displayName = user.displayName;
  const uid = user.uid;
  const photoURL = user.photoURL;

  return admin.database().ref("/users").child(uid).update({"uid":uid, "email":email,
  "displayName":displayName, "photoURL":photoURL})

})

exports.createMessagePhoto = functions.storage.object().onChange(event => {
   const object = event.data;
   const file = gcs.bucket(object.bucket).file(object.name);
   const metadata = object.metadata

   console.log("log: " + JSON.stringify(metadata));

   // Exit if this is a move or deletion event.
  if (object.resourceState === 'not_exists') {
    return console.log('This is a deletion event.');
  }

  return file.getSignedUrl({
  action: 'read',
  expires: '03-09-2500'
  }).then(signedUrls => {
    var downloadUrl = signedUrls[0]
    return admin.database().ref("/users").child(metadata.uid).once("value", function(snap) {
      return admin.database().ref("/messages").push()
      .set({"photo":downloadUrl, "typeMessage":"photo", "user":snap.val()})
    })
  });
})

exports.countLikes = functions.database.ref('messages/{$messageId}/likes').onWrite(event => {
    return event.data.ref.parent.child('likes_count').set(event.data.numChildren());
});
