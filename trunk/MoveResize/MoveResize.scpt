tell application "System Events"
    set _everyProcess to every process
    repeat with n from 1 to count of _everyProcess
        set _frontMost to frontmost of item n of _everyProcess
        if _frontMost is true then set _frontMostApp to process n
    end repeat
    
    set _windowOne to window 1 of _frontMostApp
    
    set _position to position of _windowOne
    set _size to size of _windowOne
    
    set _x to item 1 of _position
    set _y to item 2 of _position
    set _width to item 1 of _size
    set _height to item 2 of _size
    
    try
        set newBounds to do shell script "java -jar ~/Desktop/MoveResize.jar -move " & _x & ":" & _y & ":" & _width & ":" & _height
        if (length of newBounds is greater than 0) then
            set newBounds to my split(newBounds, ":")
            
            set _x to item 1 of newBounds
            set _y to item 2 of newBounds
            set _width to item 3 of newBounds
            set _height to item 4 of newBounds
            
            set position of _windowOne to {_x, _y}
            set size of _windowOne to {_width, _height}
        end if
    end try
end tell

on split(someText, delimiter)
    set AppleScript's text item delimiters to delimiter
    set someText to someText's text items
    set AppleScript's text item delimiters to {""} --> restore delimiters to default value
    return someText
end split