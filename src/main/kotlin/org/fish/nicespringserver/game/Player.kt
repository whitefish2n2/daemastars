package org.fish.nicespringserver.game

import java.util.concurrent.atomic.AtomicInteger

data class Vector3(var x: Float, var y: Float, var z: Float)

data class Player (
    var name:String,
    var id: String,

){
    var position:Vector3 = Vector3(0f,0f,0f);
    var rotation:Vector3 = Vector3(0f,0f,0f);
    var score: AtomicInteger = AtomicInteger(0);
};