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
-->
</div>
<div align="center">
    <video src="https://user-images.githubusercontent.com/55872600/199725259-f3188aee-c10b-4da8-a28e-2da716003620.mov" width=180></video>
</div>
    
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

## Consumer functionality
The current functionality that has been implemented for this library that can be changed;
* `setCustomGestureDetector(detector)` - Override the current gesture handler with a custom one by implementing the interface.
* `loadObject(resourceId)` - Load an object file locally from a raw resource. 
* `loadObject(url)` - Load an object file from a remote data source with retrofit.
* `loadMaterial(resourceId)` - Load a material file locally from a raw resource.
* `loadMaterial(url)` - Load a material file from a remote data source with retrofit.
* `setBackgroundColor(color)` - Set the view's background color to the selected color.
* `setObjectColor(color)` - Set the color of all faces from the file to the selected color.
* `setObjectGroupColor(color, group)` - Set the color of all faces in the group to the selected color.
* `setLightPosition(position)` - Set the position of the light.
* `setCameraPosition(position)` - Set the current position of the camera.
* `setCameraFrustum(near, far)` - Set the frustum cone points for the camera.
* `setObjectClickCallback(callback)` - Set a callback for whenever an object has been clicked on.
* `setIntersectionCalculationMode(mode)` - Set the current mode for calculating ray intersection for user clicks. Chosen between 2 predefined algorithms.

## Version
Current version of this repository is *1.0.0-alpha*

## License
```
MIT License

Copyright (c) 2022 ColleDK

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
