#!/bin/bash
# If you are not using Login Items UI to register the launching of MoveResize.app at the time of
# login, uncomment the following line to register it
#sudo defaults write com.apple.loginwindow LoginHook ${HOME}/scripts/MoveResize/MoveResize.app
sudo defaults write com.apple.loginwindow LogoutHook "osascript ${HOME}/scripts/MoveResize/QuitMoveResize.scpt"