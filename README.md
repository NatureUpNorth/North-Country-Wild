# north-country-wild
This is the documentation for the second version of the North Country Wild photo uploader, with the dynamic GUI.

This version of the code was written by Remi LeBlanc, Guinevere Gilman, and Corinna Pilcher in spring 2019.

The user authentification class/interface was written by Choong-Soo Lee. It passes a username/password combination to the Nature Up North website server for verification that it corresponds to a valid NUN website account.

The workflow is as follows:
	-Splash screen with information about the project
		*Clicking the "Begin" button takes user to the next screen
	-Login screen where user is prompted to enter their NUN username/password
		*Requires the user's NUN display name (not email) and their NUN password
	-Upload screen where user is prompted to enter the filepath to their pictures and information about their camera deployment, in a 	   tabbed window format
		*Uploader automatically converts lat/long data to decimal degrees if it is not submitted in that format
		*Tabs cannot be accessed until the user presses the next button
		*User cannot change tabs without filling in all the required fields on the current tab
		*Previous tabs can be navigated to with the previous button or by clicking the tab at the top of the window
		*User can submit as batches of files in a row as they like, but only one batch can be uploaded at a time
		*User must select at least one habitat description, but can only select one urbanization button
		*Submit button starts an upload thread
			>Upload thread takes all image files in indicated directory and writes metadata about them to a metadata file
			>Also included in the metadata file is the info the user included on the upload screen
			>The pictures and the metadata file are then uploaded to the dropbox client
			>The file structure for the dropbox is as follows: organization name>uploader username>time of upload
	-Loading bar window that appears when the uploader is uploading to the dropbox
		*The loading bar window includes a loading bar which grows incrementally as each file is uploaded to the dropbox
		*The loading bar window displays the file currently being uploaded in text below the loading bar
		*The loading bar window closes when the uploader finishes uploading that batch of files

Partially unimplemented features:
	-HTML entry page
	-Affiliations list
	-More error checking needs to be implemented:
		*Error checking for cancelled uploads
		*Convert longitude to be negative if it's positive when they submit
		*File path format checking
