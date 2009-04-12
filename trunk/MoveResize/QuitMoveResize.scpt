(*
 * This script is used to stop the MoveResize server.
 *
 * Author: Sandip V. Chitale
*)
on run
	try
		do shell script "echo  exit | nc 127.0.0.1 6789"
	end try
end run