# north-country-wild
This is the repository that contains python scripts for processing and uploading game camera images to the zooniverse.

## PythonScripts folder
There are several scripts in this folder, with different functions. Some are here just so that some of our Nature Up North interns have a place to see the code.

The python scripts that we use for the Game Camera images are:

- `upload_and_process_images.py` - This script has several subcommands to specialize the actions of the script.

- `upload_from_manifest.py` - This is the script that uploads images to the zooniverse

- `run_config.py` - This script accompanies `upload_from_manifest.py` when it is run.

Also in this directory is an additional script that is used for some of our preliminary work with acoustic files.

- `copy_randomly_selected_files.py` - This script is used to collect and copy a random subset of audio files for annotation. It depends on the `.csv` file called "randomly_selected_files_for_annotating.csv" that is located in the misc_files subdirectory.
