# MlKit

In this application, we will get an image from the user and get the text on that image by using the MlKit library.

It's a simple application, so we won't need an architecture to implement it.

As I mentioned before, the user can pick the photo from the storage or take a new photo with the camera.

If the user chooses to pick it from storage; we will ask for permission to access to storage.
Then, pickImage function runs and starts the intent to pick the image with the request code “PICK_IMAGE”

After that, we get the image in the ```onActivityResult()``` function (First block)

If the user takes the other option -taking a photo with the camera- we will run the ```camera()``` function. 

Inside this function, we are creating a file URI after taking the photo. Then we get the same photo with the URI that we’ve just created and put it into the imageview. 

And finally, we can detect the photo that the user put on the imageview with the ```detectImage()``` function. We check if there’s text in the photo by using ```?let``` (Scope function).

If there is, we basically detect it with the recognizer class function which is basically ‘process’. Then we toast the text on the image and that’s it! You can use that text wherever you want.
