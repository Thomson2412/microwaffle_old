class AudioManager{

    sounds = {}; 

    init(fileobjects){
        for(var i = 0; i < fileobjects.length; i++){
            var name = fileobjects[i].filename.split(".")[0];
            this.sound = document.createElement("audio");
            this.sound.src = "/sound/" + fileobjects[i].filename;
            this.sound.setAttribute("preload", "auto");
            this.sound.setAttribute("controls", "none");
            this.sound.style.display = "none";
            this.sound.volume = fileobjects[i].volume;
            this.sound.loop = fileobjects[i].loop;
            document.body.appendChild(this.sound);
            fileobjects[i]["sound"] = this.sound;
            this.sounds[name] = fileobjects[i];
        }
    }

    play(name){
        this.stop(name);
        this.sounds[name].sound.play();
    }

    stop(name){
        this.sounds[name].sound.pause();
        this.sounds[name].sound.currentTime = 0;
    }
}