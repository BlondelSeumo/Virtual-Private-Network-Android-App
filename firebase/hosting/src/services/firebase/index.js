import firebase from "firebase/app"
import "firebase/firestore"
import "firebase/auth"
import {firebaseConfig} from "@app/configs";

firebase.initializeApp(firebaseConfig)

export const firestore = firebase.firestore()
export const auth = firebase.auth();
export const fb = firebase