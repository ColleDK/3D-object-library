# 3D-object library #
3D-object library is an Android library for visualizing 3D objects natively with OpenGL ES 2.0. The current functionality build into this library consist of;
* Asynchronous loading of objects from a raw resource file or from a URL.
* Asynchronous loading/attaching of materials to the objects from a raw resource file or from a URL.
* Per-face triangulation to support all .obj files.
* Caching loaded objects locally in a SQLite DB for faster load times.
* Default gesture handler with functionality for rotation, zoom and click actions.
* Raypicking calculations by Möller-Trumbore fast intersection algorithm or tetrahedron volume algorithm.

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

### License
[MIT](https://choosealicense.com/licenses/mit/)