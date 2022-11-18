# 3D-object library #
3D-object library is an Android library for visualizing 3D objects natively with OpenGL ES 2.0. The current functionality build into this library consist of;
* Asynchronous loading of objects from a raw resource file or from a URL.
* Asynchronous loading/attaching of materials to the objects from a raw resource file or from a URL.
* Per-face triangulation to support all .obj files.
* Caching loaded objects locally in a SQLite DB for faster load times.
* Default gesture handler with functionality for rotation, zoom and click actions.
* Raypicking calculations by Möller-Trumbore fast intersection algorithm or tetrahedron volume algorithm.

<!--
<div align="center">
    <img src="https://user-images.githubusercontent.com/55872600/199725283-17e7aa4f-1853-49bc-afa3-ea5d218addc9.png" alt="visualizing objects" width=300></img>
</div>
-->
<div align="center">
    <video src="https://user-images.githubusercontent.com/55872600/199725259-f3188aee-c10b-4da8-a28e-2da716003620.mov" width=180></video>
</div>
    
## Version
[![](https://jitpack.io/v/ColleDK/3D-object-library.svg)](https://jitpack.io/#ColleDK/3D-object-library)
    
## Installation
This library is published through Jitpack.io. For adding the repository to an existing project
Add jitpack.io to your settings.gradle file
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```
Add the dependency to the repository
```
dependencies {
    implementation 'com.github.ColleDK:3D-object-library:$latest_release'
}
```

## Usage
First you should initialize the custom view `ObjectSurfaceView`, which allows you to visualize the 3D objects.
```kotlin
AndroidView(factory = { ctx -> 
  ObjectSurfaceView(ctx) 
})
```
Next are some functionality that can be applied onto this custom view.
`setCustomGestureDetector(detector)` - Override the current gesture handler with a custom one by implementing the interface.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setCustomGestureDetector(object: IGestureDetector{
      override fun handleMotionEvent(event: MotionEvent): Boolean { /* Your logic here */ }
    })
  }
})
```
`loadObject(resourceId)` - Load an object file locally from a raw resource.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    loadObject(resourceId = R.raw.someobject, objectName = "something" /* For storing the object in the database */ )
  }
})
```
`loadObject(url)` - Load an object file from a remote data source with retrofit.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    loadObject(url = "https://localhost:8080/someobject.obj", objectName = "something" /* For storing the object in the database */ )
  }
})
```
`loadMaterial(resourceId)` - Load a material file locally from a raw resource.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    loadMaterial(resourceId = R.raw.somematerial, materialName = "something" /* For storing the materials in the database */)
  }
})
```
`loadMaterial(url)` - Load a material file from a remote data source with retrofit.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    loadMaterial(url = "https://localhost:8080/somematerial.mtl", materialName = "something" /* For storing the materials in the database */)
  }
})
```
`setBackgroundColor(color)` - Set the view's background color to the selected color.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setBackgroundColor(color = Color(1f, 1f, 1f, 1f)) // Set by color object
    setBackgroundColor(color = floatArrayOf(1f, 1f, 1f, 1f)) // Set by float array
  }
})
```
`setObjectColor(color)` - Set the color of all faces from the file to the selected color.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setObjectColor(color = floatArrayOf(1f, 1f, 1f)) // Set by float array
  }
})
```
`setObjectGroupColor(color, group)` - Set the color of all faces in the group to the selected color.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setObjectGroupColor(color = floatArrayOf(1f, 1f, 1f), groupName = "somegroup") // Set by float array
  }
})
```
`setLightPosition(position)` - Set the position of the light.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setLightPosition(x = 1f, y = 1f, z = 1f)
  }
})
```
`setCameraPosition(position)` - Set the current position of the camera.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setCameraPosition(x = 1f, y = 1f, z = 1f)
  }
})
```
`setCameraFrustum(near, far)` - Set the frustum cone points for the camera.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setCameraFrustum(near = 1f, far = 50f)
  }
})
```
`setObjectClickCallback(callback)` - Set a callback for whenever an object has been clicked on.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setObjectClickCallback(callback = { hasHit, hitGroupName -> /* Your logic here */ })
  }
})
```
`setIntersectionCalculationMode(mode)` - Set the current mode for calculating ray intersection for user clicks. Chosen between 2 predefined algorithms.
```kotlin
AndroidView(factory = { ctx ->
  ObjectSurfaceView(ctx).apply {
    setIntersectionCalculationMode(mode = IntersectionMode.MOLLER_TRUMBORE)
  }
})
```

## License
```
MIT License

Copyright (c) by 2022 ColleDK (William Mortensen)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
