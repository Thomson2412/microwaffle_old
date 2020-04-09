let sounds = {};

class AudioManager{

    init(fileObjects){
        for(let i = 0; i < fileObjects.length; i++){
            let name = fileObjects[i].filename.split(".")[0];
            this.sound = document.createElement("audio");
            this.sound.src = "/sound/" + fileObjects[i].filename;
            this.sound.setAttribute("preload", "auto");
            this.sound.setAttribute("controls", "none");
            this.sound.style.display = "none";
            this.sound.volume = fileObjects[i].volume;
            this.sound.loop = fileObjects[i].loop;
            document.body.appendChild(this.sound);
            fileObjects[i]["sound"] = this.sound;
            sounds[name] = fileObjects[i];
        }
    }

    play(name){
        this.stop(name);
        sounds[name].sound.play();
    }

    stop(name){
        sounds[name].sound.pause();
        sounds[name].sound.currentTime = 0;
    }
}