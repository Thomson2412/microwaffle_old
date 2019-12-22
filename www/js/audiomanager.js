class AudioManager{

    sounds = {}; 

    init(filenames){
        for(var i = 0; i < filenames.length; i++){
            var splitName = filenames[i].split(".")[0];
            this.sound = document.createElement("audio");
            this.sound.src = "/sound/" + filenames[i];
            this.sound.setAttribute("preload", "auto");
            this.sound.setAttribute("controls", "none");
            this.sound.style.display = "none";
            document.body.appendChild(this.sound);
            this.sounds[splitName] = {"name": splitName, "sound": this.sound};
        }
    }

    play(filename){
        this.stop(filename);
        this.sounds[filename].sound.play();
    }

    stop(filename){
        this.sounds[filename].sound.pause();
        this.sounds[filename].sound.currentTime = 0;
    }
}