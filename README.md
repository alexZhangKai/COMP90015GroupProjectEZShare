# [COMP90015](https://handbook.unimelb.edu.au/subjects/comp90015/) Distributed Systems

## Project 2

### Semester 1, 2017 - University of Melbourne

---

#### Main classes

```EZShare.Server``` and ```EZShare.Client```

#### Specifications

- [Project 1](specs/proj1.pdf)
- [Project 2](specs/proj2.pdf)

#### Team: AALT | Members

- [AB](https://github.com/abhineet-gupta)
- [Alex](https://github.com/alexZhangKai)
- [Lisha](https://github.com/Lisharabbit)
- Titus
---

#### Sample Runs

- **Start the server**, in debug mode with its own IP address provided

    ````java -cp ezshare.jar EZShare/Server -debug -advertisedhostname 100.98.55.22````

- **Publish** a resource to server via client, using secure channel

    ```java -cp ezshare.jar EZShare/Client -debug -publish -uri http://remote-pub-res1.com -host 100.98.55.22 -secure```

- **Share a file** on the server, using unsecure channel

    ```java -cp ezshare.jar EZShare/Client -debug -share -host localhost 100.98.55.22 -uri file:///D:/GDrive/Docs/2017/Code/ds-ezshare/serverFile/testFile.png -secret 279002d56d0644ec94e1c1c1ed56738a```

- **Exchange** list of other servers with a given server, securely

    ```java -cp ezshare.jar EZShare/Client -debug -secure -host 100.98.55.22 -exchange -servers 100.98.58.247:3781```