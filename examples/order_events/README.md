See Use Case doc https://docs.google.com/document/d/1nAitLVfkkwKq0ginm4LSMuAxOIUv5JdGphWk7gPl5oo.

## Transform Kaggle data into event data:
The prepared data is already generated in the `./rawdata` folder.

If you want to change the `prepare.py` script and generate it again:

    # make your changes to prepare.py
    python3 -m pip install - sourcedata/requirements.txt
    python3 ./sourcedata/prepare.py

Check the generated `./sourcedata/data.csv`.
If it's okay for you, move it to the `rawdata` folder

    cp ./sourcedata/data.csv ./rawdata/data.csv
