import json
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.animation as animation
from matplotlib.animation import PillowWriter

# load json file
with open("data/gesture_1761839901287.json") as f:
    data = json.load(f)

# point by point
points = data['points']
x = [p['x'] for p in points]
y = [p['y'] for p in points]
time = [p['time'] for p in points]
pressure = [p['pressure'] for p in points]
dx = np.diff(x)
dy = np.diff(y)
dt = np.diff(time)
velocity = np.sqrt(dx**2 + dy**2) / dt  # px/ms

fig, chart = plt.subplots(2, 2, figsize=(12,12))

# trajectory
chart[0][0].plot(x, y, marker='o', color='blue')
chart[0][0].set_title("Gesture trajectory")
chart[0][0].set_xlabel("x")
chart[0][0].set_ylabel("y")
chart[0][0].invert_yaxis()  # so it looks like it was draw  on android

# pressure
chart[1][0].plot(pressure, color='red')
chart[1][0].set_title("Fingerpress")
chart[1][0].set_xlabel("point")
chart[1][0].set_ylabel("pressure")

# velocity
chart[1][1].plot(velocity, color='green')
chart[1][1].set_title("Gesture velocity")
chart[1][1].set_xlabel("segment")
chart[1][1].set_ylabel("velocity (px/ms)")

# drawing animation
chart[0][1].set_xlim(min(x)-10, max(x)+10)
chart[0][1].set_ylim(min(y)-10, max(y)+10)
chart[0][1].invert_yaxis()
line, = chart[0][1].plot([], [], 'bo-')

def update(num):
    line.set_data(x[:num], y[:num])
    return line,

ani = animation.FuncAnimation(fig, update, frames=len(x), interval=50)

#ani.save("gesture.gif", writer=PillowWriter(fps=20))

plt.tight_layout()
plt.show()