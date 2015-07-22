# SpaceCombatBilliards

A multiplayer 3D flying/combat game written in java.  Graphics courtesy of LWJGL.  Can be played by computers connected to the same network.  Incorporates a simple 3D physics engine, multi-thread programming on the client side, some basic networking, and quaternion-based flight mechanics.

Instructions:

Run the server file and enter the number of players you plan on having in your game (at least 2).

Each player should run the client file on his/her computer.  When the fiery red intro screen pops up, press enter (if you would like to use arrow key controls for flying) or click the screen (if you would prefer to use mouse controls). Arrow key controls are recommended to start out with.

Wait until all clients are connected to the server (this will only work if the clients are connected to the same network).  When that happens, you'll find yourself floating around in a green box.  Start using controls and your ship will appear. 

Note: the game runs pretty slow if your connection strength isn't perfect or if the client computers have too many other processes running.  Playing with too many players also slows things down.


Controls:

1. Arrow keys or mouse to turn (see above)
 
2. Q and A to speed up and slow down

3. Space to charge up and release to shoot a cue ball

How it works:
The goal of the game is to hit enemy ships with billiard balls.  The catch: you can't fire billiard balls - only cue balls! (No damage is done by hitting the enemy with your cue ball).  So, in order to attack your opponents, you must aim your cue ball at a billiard ball so that it directs the billiard ball toward the enemy ship (just like in actual billiards).  Now, doing this is nearly impossible without using the heat-seeking feature.  To activate heat-seaking, point your aimer at another ship.  The aimer should turn red, which means you're locked on to the ship.  Now, if you shoot at a billiard ball, and if you hit it so that it's moving in the general direction of the ship that you've locked on to, it will curve towards the enemy.  Once a ship has been hit five times, it is destroyed.  The game ends when all but one ship are destroyed.
