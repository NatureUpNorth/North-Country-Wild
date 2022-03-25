"""
This script is written in 64 bit Python 3.10, though it should run in any Python 3+ environment with the
following packages:
    - panoptes_client version 1.4
    - for Windows 64 bit and recent Mac OS's install python-magic-bin after the panoptes-client is installed.
    - For Windows 32 bit environments running on a AMD 64 bit processor, install python-magic-Win64
    after installing panoptes_client
    - a copy of the script run_config.py must be included in the working directory with this script
    - run_config.py requires the package cryptography at version 36.0+

The first time this script is run, run_config.py will be called up to to build a file run_config.csv.
This will ask four questions - for the directory of where the project data files reside - generally where
you will keep this script and other files associated with the project (though it is not specifically used
for this script), your zooniverse username and zooniverse password, and finally the project slug for your
project.  The run_config.csv file will store your password in an encrypted format.  To retrieve the password
in plain text to send it to zooniverse the exact run_config.py file used to encrypt it is required  - anyone
with BOTH the run_config.csv and the exact run_config.py used to create it can retrieve the password, but
both the matching pair are needed.  Deleting the run_config.csv file will result in a new file being built
the next time this script is run, and run_config.py will be updated with with the new encryption key.

This script runs in an interactive mode, asking first for the full path to the directory where the images
and manifest reside.  If the script is run from that directory (ie there is a copy of it and run_config.py
in the image directory) then a single '.' will define that directory as the working directory, otherwise
either the full path from the root or from the working directory where this script is run must be specified.

Next the script will ask for the name of the manifest file to be used, defaulting to "manifest.csv" if a single '.'
is entered. Next the script will ask for the name of the subject set to use, either creating it, or pausing
to acquire a list of existing subject if the subject set already exists.  This step queries zooniverse and is
slow. Generally one would not add new subjects to an existing set with many subjects (ie >10,000) already
uploaded unless it is to recover from an large upload that was interrupted - in which case just give it time....

Finally you are asked if you want a summary file build at the end of the upload process showing all the subjects
in the subject set with their zooniverse id and metadata in a format similar to the manifest.  This process
acquires the information directly from zooniverse and will be slow, but it shows exactly what is in zooniverse,
not what was intended...

Once any previous subjects are acquired, the script walks through the manifest and determines if the subject
was previously uploaded. If not, ALL the image files listed in each line of the manifest are added to the list
of image locations to upload to the subject and the metadata for the subject is prepared.  Files that are
listed in the manifest but not found are reported but are otherwise skipped - a subject will be created with
the files that are found unless no files are found at all.

Notes on the Manifest file: Python and zooniverse will assume the file is in strict utf-8 encoding and is
comma delimited. Be aware some spreadsheets and OS will use different encoding (ex windows cp1252)  Generally
these will work unless there are non-ascii characters.  This script as written, assumes there will be at least
two columns in the manifest and the combination of the first and second column values is unique for every
subject to be created. If this is not the case, an additional column can be added to the manifest or all the
lines in this script with the text 'previous_subjects' in them need to be carefully reviewed and modified.

Once the subjects are prepared, the upload step occurs when the subject.save() is executed.  One can comment
out that line and the next subject_set.add(subject.id), and run the script as a "dry run".  The script will
report any issues just as if it is uploading (though much faster).   The script will report the number of lines
in the manifest, the number of subject previously uploaded and found in the subject set, and the number of
subjects it would have created.  These should agree.  Once the dry run is error free, uncomment the two lines
and repeat running the script.

If the upload stops at any time (usually connection issues with zooinverse), restart it with the same directory
and subject set name and give it time to sort out what has and has not been uploaded to zooniverse - this can
be slow, but the script should resume where it ended off without duplicating subjects.  Even single subjects
can be deleted and re-uploaded if errors are found after the fact. Keep in mind it uses the first TWO columns
of the manifest to determine if a subject previously exists.

Finally when the upload is complete, the script builds the summary file if it was requested.  The summary
file is placed in the image directory under the name  'Uploaded_' + set_name + '_' + current date + ',csv'.
"""

import csv
import sys
import os
from os.path import join
from datetime import datetime
import panoptes_client
from panoptes_client import SubjectSet, Subject, Project, Panoptes
from run_config import Runconfig  # this is optional - if the run_config option is to be used the script
# run_config.py must be in the working directory with this script.  Otherwise the required info can be
# hardcoded in lines 78, 120, 121.

project = Project.find(slug=Runconfig().project_slug)

# if the run_config option is to be used the script run_config.py must be in the working directory with this script.

# Input path and directory where images and manifest reside;
while True:
    manifest_folder = input('Enter the full path for the image directory where the manifest resides, or enter "." '
                            'to use the current directory' + '\n')
    if manifest_folder == '.':
        manifest_folder = os.getcwd()
    if os.path.exists(manifest_folder):
        break
    else:
        print('That entry is not a valid path for an existing directory')
        retry = input('Enter "y" to try again, any other key to exit' + '\n')
        if retry.lower() != 'y':
            quit()

# Input manifest file name. If it is the default "manifest.csv" enter "." :
while True:
    manifest_file = input('Enter the file name for the manifest including the extension, or enter "." '
                          'to the default "manifest.csv"' + '\n')
    if manifest_file == '.':
        manifest_file = 'manifest.csv'
    manifest_file = join(manifest_folder, manifest_file)
    if os.path.isfile(manifest_file):
        break
    else:
        print(manifest_file, 'Not found')
        retry = input('Enter "y" to try again, any other key to exit' + '\n')
        if retry.lower() != 'y':
            quit()

#  Input subject set name to be created or used:
set_name = input('Entry a name for the subject set to use or create:' + '\n')

#  test for the optional summary output
summary = False
if input('Enter "y" to save a summary file, any other key to exit' + '\n').lower() == 'y':
    summary = True

# set up zooniverse User_name and password, and project slug from run_config.py
Panoptes.connect(username=Runconfig().username, password=Runconfig().password)
project = Project.find(slug=Runconfig().project_slug)

# open the manifest and get the header
with open(manifest_file, 'r') as mani_file:
    manifest = csv.reader(mani_file)
    header = manifest.__next__()

# This section sets up a subject set and if the subject set exists, lists the contents
previous_subjects = []
previous_subject_count = 0
try:
    # check if the subject set already exits
    subject_set = SubjectSet.where(project_id=project.id, display_name=set_name).next()
    print('Please wait while a list of previous subjects uploaded to this set is prepared')
    print('This can take approximately one minute for every 400 subjects previously uploaded')
    for subject in subject_set.subjects:
        previous_subject_count += 1
        if (previous_subject_count - 1) % 100 == 0:
            print('.', end='')
        try:
            # build list of contents using the first two columns of the manifest as an identifier
            previous_subjects.append(subject.metadata[header[0]] + ' ' + subject.metadata[header[1]])
        except KeyError:
            continue
    print('\n' + 'Acquired list of', previous_subject_count, 'previous subjects' + '\n')
except StopIteration:
    # create a new subject set for the new data and link it to the project above
    subject_set = SubjectSet()
    try:
        subject_set.links.project = project
        subject_set.display_name = set_name
        subject_set.save()
    except panoptes_client.panoptes.PanoptesAPIException:
        print('Failed to create subject set', str(sys.exc_info()[1]), 'Check username and password')
        print('Delete the file run_config.csv in the working directory and rerun the script.')
        quit()

# This section adds subjects from the manifest to the above subject set
file_types = ['jpg', 'jpeg']  # for some projects this list may be expanded to include other media types
lines_in_manifest = 0
subjects_created = 0
previously_uploaded = 0

with open(manifest_file, 'r') as mani_file:
    r = csv.DictReader(mani_file)
    for line in r:
        lines_in_manifest += 1
        # test if subject previously uploaded based on the contents of the first two columns in the manifest
        if line[header[0]] + ' ' + line[header[1]] not in previous_subjects:
            number_of_images = 0
            metadata = {}
            # prepare subject and link to project
            subject = Subject()
            subject.links.project = project
            # walk through the manifest columns and add locations for all image files
            for item in header:
                if line[item].partition('.')[2].lower() in file_types:
                    # test file exists:
                    if os.path.isfile(join(manifest_folder, line[item])):
                        number_of_images += 1
                        # file location added and metadata for it is defined:
                        subject.add_location(join(manifest_folder, line[item]))
                        subject.metadata[item] = line[item]
                    else:
                        print('For subject', line[header[0]] + ' ' + line[header[1]], 'File', item, 'Not found')
                # for all items that are not files to be uploaded, define the metadata for that item -
                # note the test for various blank or empty column items - this is an optional step = these could
                # be left the same as the manifest for that field (replace the elif with a simple else:)
                elif line[item] and line[item] != ' ' and line[item] != 'NA':
                    metadata[item] = line[item]
            # for subjects with any files found to upload, update the subject metadata and do the subject.save()
            if number_of_images > 0:
                subject.metadata.update(metadata)
                print('Uploading subject for', line[header[0]] + ' ' + line[header[1]])
                #  the next two line do the actual upload to zooniverse and may be commented out for testing
                subject.save()
                subject_set.add(subject.id)
                subjects_created += 1
        else:
            previously_uploaded += 1
print('From', lines_in_manifest, 'lines in the manifest, there were', previously_uploaded,
      'previously uploaded subjects found and', subjects_created, 'subjects_created')

# the optional summary output
if summary:
    print('Preparing summary file, please wait...')
    uploaded = 0
    with open(manifest_folder + os.sep + 'Uploaded_' + set_name + '_' + str(datetime.now())[0:10]
              + '.csv', 'w', newline='') as file:
        fieldnames = ['zoo_subject']
        fieldnames.extend(header)
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        subject_set = SubjectSet.where(project_id=project.id, display_name=set_name).next()
        for subject in subject_set.subjects:
            uploaded += 1
            if (uploaded - 1) % 100 == 0:
                print('.', end='')
            new_line = subject.metadata
            new_line['zoo_subject'] = subject.id
            writer.writerow(subject.metadata)

        print('\n', uploaded, ' subjects found in the subject set, see the full list in  '
                              'Uploaded_' + set_name + '_' + str(datetime.now())[0:10] + '.csv')
