const functions = require('firebase-functions');
const cors = require('cors')({origin: true});   
const admin = require('firebase-admin');
var serviceAccount = require('./firebase-admin.sdk.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: ""
});

exports.removeUser = functions.https.onRequest((request, response) => {
    cors(request, response, () => {
        response.set('Access-Control-Allow-Origin', '*');
        response.set('Access-Control-Allow-Methods', 'GET, PUT, POST, OPTIONS');
        response.set('Access-Control-Allow-Headers', '*');
    
        if(request.method === 'POST') {
            const {email} = request.body;
            admin.auth().getUserByEmail(email)
            .then(function(u) {
                const {uid} = u;
                admin.auth().deleteUser(uid)
                .then(function() {
                    response.status(200).send({message: 'Successfully deleted user'});
                    functions.logger.info({message: `Successfully deleted user uid: ${uid}`});
                })
                .catch(function(error) {
                    response.status(422).send({error: error});
                    functions.logger.info(`Error deleting user at uid: ${uid} & error: ${error}`);
                });
            })
            .catch(function(error) {
                response.status(422).send({error:error});
                functions.logger.info('Error fetching user data:', error);
            });  
        }else {
            response.end()
        }

    })
});