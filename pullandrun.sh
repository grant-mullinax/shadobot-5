cd ~/shadobot-5
tmux new -d -s shadobot-5
tmux send-keys -t shadobot-5 "git pull" ENTER "sudo ./gradlew -stop" ENTER "sudo ./gradlew run" ENTER
