package io.reco

object ArticleExample {
  val value: String = "I’ve got a fever, and the only prescription is more points dancing across my screen. In a previous blog" +
    " post, I covered how to animate thousands of points using HTML5 canvas and d3, but that approach doesn’t scale " +
    "too well beyond 10,000 points. Perhaps you can push it to 20,000 if you don’t mind dropping a frame here or " +
    "there, but if you need that buttery smoothness, you’re going to want to switch from the 2D canvas to using WebGL." +
    " And who doesn’t need buttery smoothness? Now I know what you’re thinking, WebGL? I know what Web is, but that GL " +
    "business? Sounds terrifying. And for good reason, it’s a damn nightmare in here, but hey we need more points! We" +
    " can’t give up now. Luckily, a few brave souls have written abstractions on top of WebGL to make it easier to work" +
    " with. In this post, I’ll explore using regl, one such library by Mikola Lysenko to help do the heavy lifting." +
    " Take note that I am a data visualization guy, not a 3D graphics guy, so my approach is based on my experience" +
    " in that area. The problem with sticking with the 2D canvas is that we typically have a big old for loop" +
    " iterating over each point and drawing it on screen with something like context.fillRect(). Since we want" +
    " to maintain frame rate of 60 frames per second, we’re going to need to complete all of our updating and " +
    "drawing before the browser wants to draw the next frame. With 60 frames a second, that works out to less " +
    "than 17ms to do all of our calculations and updates. Once we get enough iterations in our loop, we’re" +
    " going to exceed that threshold and end up dropping frames occasionally, making for a jumpy and far less" +
    " satisfying animation. By switching to using WebGL, we can pass over some of the effort spent iterating " +
    "over points from the CPU to the GPU. The way we do that is with shaders. Luckily for us (perhaps), regl " +
    "makes it really easy to dive right in and start playing around with them. Look, I’m no WebGL or shader" +
    " expert, I just want swarms of points to storm across my screen. If you really want to learn how shaders " +
    "work, you should probably read some other resource like the Book of Shaders. Alternatively, you can stare" +
    " at the examples at regl.party while banging your head against the wall until it begins to make a little " +
    "sense (like I did). With that disclaimer in mind, here’s a basic rundown. You’ve got two types of shaders:" +
    " a vertex shader and a fragment shader. Shaders are just programs that run on the GPU, typically to produce" +
    " some graphical output. A vertex shader updates a special value called gl_Position that determines" +
    " where a vertex is positioned on screen. A fragment shader updates a special value called gl_FragColor" +
    " that determines which color a pixel will be. Vertex shaders are called for all points or vertices passed" +
    " in and fragment shaders for all pixels. That covers a very basic (and almost my complete) understanding " +
    "of shaders. Disappointing, right? I know and I’m sorry. Turns out, however, that’s almost all you need to " +
    "know to get started, so let’s try drawing with them before we get to animating. Here’s a link to a working " +
    "example. If you want to take a look at all the example code at once, check it out. Now before we get to anything" +
    " too fancy, let’s start by defining a few constants and creating our “dataset” of 100,000 points. Ok, so we" +
    " have some points. Notice anything a bit strange? The colors are arrays! Colors in WebGL are formatted as" +
    " arrays of length four (red, green, blue, alpha) with values ranging from 0 to 1 instead of their typical" +
    " string format used in HTML and CSS. The code above produces points with colors ranging from rgb(0, 0, 0) " +
    "to rgb(0, 255, 0). Since we’re not dealing with alpha in this example, I’ve excluded the fourth value from" +
    " the array (we’ll fill it in as 1 later). The x and y values that we specified will range from 0 to width " +
    "and 0 to height, also known as pixel space. This coordinate system is what we’re used to when working with" +
    " normal canvas or svg, but in WebGL everything lives in normalized device coordinates, which is a fancy way" +
    " of saying the top left corner is at (-1, 1) and the bottom right corner is at (1, -1). We’ll keep the width" +
    " and height variables around so we can scale our x and y positions appropriately in our shader. So we have a " +
    "collection of points, now let’s set up the regl drawing loop before diving into the nitty gritty of the" +
    " shaders. Not much going on here: we clear the background with the color black and then we call drawPoints" +
    " with a few properties, but with this code we now have a loop running 60 times a second and attempting to " +
    "draw points on the screen. All right, since regl is really focused around shaders, all of the work comes out" +
    " when writing shader code. We can create our drawPoints function, which will essentially connect all the" +
    " information to our shaders in a way WebGL understands. Thanks regl! I really didn’t want to do that myself." +
    " Ok, with all that aside, let’s dive into the shaders. To specify the fragment shader, we write it as a " +
    "multi-line string. We’ll keep this one as simple as we can and just set the pixel color to whatever the" +
    " vertex shader passed in. Note that varying basically means the vertex shader populates the value of this " +
    "variable. Also recall how we only stored colors as arrays of length 3 (vec3), so we append a 1 to the end" +
    " for the alpha channel. The vertex shader is a bit more complex, but the basics are shown below: First, we" +
    " update gl_PointSize, a special value that determines the size points are rendered on screen based on our" +
    " property pointWidth. Then we save the attribute color into the varying fragColor so that the fragment shader" +
    " can read its value since it does not have access to attributes. Finally, we update gl_Position to indicate" +
    " where the vertex is positioned. Now if you’ll recall, the points in WebGL space are not the same as in normal" +
    " screen pixel space– they range from (-1, 1) to (1, -1). Since we laid out our point positions in pixel space, we’ll need to normalize them in our shader. Here’s the full code including normalization: That’s right, you can create helper functions in shaders (like we did with normalizeCoords() above) and call them within your main() function. Pretty cool stuff. At this point, we have all the necessary parts to draw 100,000 points on screen with regl and shaders. Check out the demo and full code here. Here’s the live demo and full source code to get this initial animation working. So, getting started drawing 100,000 points was pretty complicated. Luckily the jump from there to animating is relatively small. At this point we already have the GPU normalizing positions of the points and drawing them on screen. And we have a loop running just waiting to animate, so what’s our strategy going to be? I’m going to take a very similar approach to what I did for animating thousands of points on canvas, except this time all the interpolation of position is going to happen in a shader so we can use the GPU. A layout algorithm can be any function that sets the x and y attribute on our point objects. The reason I’ve taken this approach is that it is a fairly common style to find in layout algorithms already made for D3, the industry standard library for data visualization on the web. For simplicity in these examples, I’ll also set color so we can animate it easily. Here’s an example layout algorithm that randomly positions points: Now when we animate between the points, all we need to do is keep track of their start position (let’s call it sx, sy) or where they currently are, and their end position (tx, ty) based on wherever their new layout places them. The important part here is now our points contain both their start and end positions, not just their current position. This will allow the shader to interpolate between them easily. The basic shape of a point object is as follows: We need to update our callback in regl.frame so that it gets the duration and start time of the animation, and knows when it can switch to the next animation. The changes are relatively simple: we keep track of the time when the animation began so we can compute time elapsed, we pass in duration and start time as new props to our drawPoints function, and we check if we’ve exceeded the duration of the animation and if so, switch to the next layout. Previously we had attributes that stored the current position and color of each point, but now we want to work with both their start and end values. To do so, we’ll modify our attributes as follows: We’ll also need a couple new uniforms that contain information about the animation as a whole. In particular, we care about how much time has elapsed between the current frame and the beginning of the animation and how long the entire animation should take. Here, duration will be another prop we pass in that represents the time for the animation to run in milliseconds. elapsed is a bit trickier - its value will be computed for each frame based on the function specified. The first argument time is populated by regl itself, while the second argument startTime is a prop that we are going to pass in. Note that time in regl is computed in seconds, so we multiply by 1000 to get milliseconds. We’ve reached the point where we have to dive back into our shader to update it to animate the points. Luckily for us, the changes are relatively small. In fact, we do not have to change our fragment shader at all! However, the vertex shader needs to be updated to know how far through the animation it is, and then to interpolate the start and end positions and colors accordingly. Since we have elapsed and duration being passed as uniforms, we can compute how far through the animation we are by simply dividing them: elapsed / duration (and maxing out at 1). We can then use the amazingly useful mix(a, b) function which linear interpolates any two values – even vectors – to figure out our current positions and colors. Let’s see how this shakes out inside our main() function: Besides declaring the new variables at the top of the shader, those are the only changes we have to make! What a beautiful day. Sadly, if you looked at the animation at this point, it would be a bit boring since we left all the magic of easing out. There’s this amazing module system of pre-built shader helpers called glslify that contains a bunch of easing functions you can drop into your code, but for now we’ll write our own. Let’s take the cubic-in-out easing code from d3-ease and use it to create a new function in our shader: All we have to do now is apply that function to our t value and we’ll have some smoothly eased animation taking place and we’re done! Here’s the full animation vertex shader: Once again, here’s the live demo and full source code of this animation. So there you have it, simple point based animation in regl. We covered how to draw 100,000 points and a basic approach to animating them. However, with these same ideas, you can do even cooler animations when you start using different layouts. Here’s an example of animating through a set of visually pleasing layouts, similar to what was done in my canvas post: And another example that uses actual data values for laying out the points: Hope you enjoyed it! Feel free to reach out to me on twitter @pbesh if you have any questions or comments."
}
