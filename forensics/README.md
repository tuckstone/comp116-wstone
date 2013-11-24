# Assignment 5: Forensics

Ashley Hedberg and Tucker Stone  
Comp 116: Intro to Computer Security  
November 24, 2013  

## Part 1
Firstly, diff was run to compare each file against the others. b.jpg and c.jpg
were found to be the same, but a.jpg was different from both of them.

The following was run for each of the provided .jpg files: steghide extract -sf
[filename]

When the passphrase prompt appeared, the enter key was pressed (using the empty
string "" as the passphrase). For files b.jpg and c.jpg, no data could be
extracted using that passphrase. However, for file a.jpg, the file prado.jpg 
was extracted. a.jpg contained a photo of Norman Ramsey hidden inside a photo 
of Norman Ramsey.

<!--- It should be noted that this was overheard by a particular classmate 
*cough* Nicholas Andre *cough* who was talking a bit too loudly about his
forensics work in 111. Seemed silly not to try it. Worked like a charm! -->

## Part 2
1. This SD card has two partitions:
  * A Win95 FAT32 partition (sectors 1 to 125001)
  * A Linux partition (sectors 125001 to 15398839)

  [Autopsy](http://www.sleuthkit.org/autopsy/) was used to determine this.

2. There does not appear to be a phone carrier involved. This seems to be the
image of a Raspberry Pi, as the FAT32 partition contains applications designed 
to run on that device.

3. The Kali Linux 1.0 operating system is being used. This was determined by
opening the /etc/\*\_version file. On this SD card, it was named
/etc/debian\_version.

4. The following applications are installed. Autopsy was used to browse through
the SD card's filesystem.
  * Windows partition:
    * [OpenGL ES 1.x](http://www.khronos.org/opengles/1_X/), an API for 2D/3D
    graphics on embedded systems - found evidence in $OrphanFiles folder
  * Linux partition:
    * Burpsuite - found in /usr/bin
    * Metasploit - found in /opt
    * Wireshark - found in /etc
    * [Unicornscan](http://www.unicornscan.org/) - found in /etc
    * Tor - found in /etc
    * [Iceweasel](https://wiki.debian.org/Iceweasel) - found in /etc
    * TrueCrypt - found in /root/.TrueCrypt
    * FCrackZip - found example files in /usr/share/doc/fcrackzip
    * ImageMagick - found in /etc
    * [ConsoleKit](http://www.freedesktop.org/wiki/Software/ConsoleKit/) - 
    found in /etc
    * [BeEF](http://beefproject.com/) - found in /etc
    * John the Ripper - found in /etc
    * [Lynis](http://www.rootkit.nl/projects/lynis.html) - found in /etc
    * Ettercap - found in /etc
    * [Cowsay](http://en.wikipedia.org/wiki/Cowsay) - found in /usr/games
    * [XSSer](http://xsser.sourceforge.net/) - found in /usr/local/bin
    * [Bluelog](http://www.digifail.com/software/bluelog.shtml) - found in
    /usr/share
    * [JavaSnoop](https://www.aspectsecurity.com/research/appsec_tools/
    javasnoop/) - found in /usr/share
    * [JBoss Autopwn](https://github.com/SpiderLabs/jboss-autopwn) - found in 
    /usr/share 
    * [Powersploit](https://github.com/mattifestation/PowerSploit) - found in
    /usr/share

5. Yes. The root password is "toor". This was discovered by running John the
Ripper against the /etc/passwd and /etc/shadow files recovered using Autopsy 
against many wordlists. This process was automated using a Python script. The 
ipmi\_passwords.txt wordlist cracked the password.

6. We do not believe that there are additional user accounts on the system. The
/home folder contains no other user directories, which we would expect to see.

7. In the /root/Pictures directory, there are ten old photos of Celine Dion. In
/root/Documents, there are two setlists and a list of upcoming tour dates for 
Celine Dion, as well. This was determined using Autopsy.

8. The suspect tried to delete files before his arrest. Autopsy found 203,552
deleted files on the SD card. Nearly everything on the FAT32 partition had been
recently deleted.

  [Photorec](http://www.cgsecurity.org/wiki/PhotoRec) was used to recover as
many deleted files as possible from the disk image. The recovered files were
stored in 48 different directories when run on a Windows laptop. These
directories were added to a new library so that their contents could be viewed
and filtered as a group. Many interesting files were discovered:
  * 36 PDF documents, including:
    * The libcurl manual
    * An email confirming a Ticketmaster order for Celine Dion tickets
    * The TrustedSec Social-Engineer Toolkit User Manual
    * The Metasploit Pro Passive Network Discovery Quick Start Guide
  * 13 JPEG images, including:
    * Two of Celine Dion
    * One of Ruby on Rails consultant Jaime Iniesta
    * Logos for Rapid7, Metasploit, and BeEF
    * Screenshots of setting up a Metasploit scan
  * 585 PNG images, including:
    * Screenshots of a Metasplot Pro project called Phishing belonging to an
    account named thao with two campaigns (Malicious PDF and USB)
    * Screenshots of other Metasploit Pro projects belonging to msfadmin and
    tdoan
    * A Rapid7 Metasploit Pro report
    * Configuration of a Nexpose module
    * An illustrated picture of a character with blue hair and green goggles
    * A diagram dissecting the string "(((a)))" into "a"
    * A picture of what appears to be a father and daughter on an amusement
    park ride
    * A logo for "Maniac: Home of the Social Engineer Tool-Kit"
    * A screenshot indicating that Metasploit was uninstalled
  * 26 application files, the names of which were mangled by PhotoRec
  * Many source code files, including:
    * 432 C files
    * 448 H files
    * 107 Java Class files
    * 2,799 Java files
    * 147 PHP files
    * 370 Ruby files
    * 378 shell scripts
    * 256 Python files
    * 2 Windows batch files
  * Four files that Windows believed to be in Microsoft Word format. None would
  open, but one contained the comment "This installer database contains the 
  logic and data required to install Foobar 1.0."

9. There are encrypted files on this system. TCHunt was used to search the
Linux partition of the SD card for TrueCrypt files. The source was downloaded
and compiled for Linux, and the Linux partition of the card was mounted so that
it could be searched. /root/.Dropbox.zip was determined to be a suspect file.

10. Yes. According to the file receipt.pdf in /root, the suspect went to see
Celine Dion at the Colosseum at Caesars Palace in Las Vegas, NV on July 28 2012.
The ticket was sold to Ming Chow. This was accomplished by using PhotoRec
on sdcard.dd to recover many image/pdf files. Browsing through the recovered 
files, a PDF containing the email receipt of this purchased ticket was found.

11. There are many strange things on this filesystem. The presence of [Kali
Linux](http://slashdot.org/topic/bi/kali-linux-the-ultimate-penetration-
testing-tool/) is suspicious. This operating system is designed for
digital forensics and penetration testing, which one would not expect to see on
a device belonging to your average celebrity stalker. This OS comes preloaded
with many forensic and penetration testing tools that a typical celebrity
stalker would not need, so something fishy seems to be going on. The 
screenshots and reports of Metasploit scans only add to this weirdness. It
seems likely that the suspect is using this device for more than celebrity
stalking.

12. The suspect is stalking Celine Dion.

