# SpikeServe
Dirty dirty server.

## Uses
Spiking out a basic HTTP server.

## Configuration
Navigate to the root directory and execute:··
`gradle build`

To start the server execute:··
`java -jar build/libs/learn-sockets-0.1.0.jar`

The server can be found at:
`localhost:5000`

## How to demo GET/POST/PUT/DELETE
Use [Postman](https://www.getpostman.com/) for POST, PUT, and DELETE methods.

Follow the steps below:

__GET__  
`localhost:5000/form`

__POST__  
POST the following format to `localhost:5000/form`
```
{
  "id"=1,
  "name"="Jane",
  "age"=25
}
```

__GET__  
`localhost:5000/form`  
Posted data will be the response.

__PUT__  
PUT the following (with the same id) to `localhost:5000/form`
```
{
  "id"=1,
  "name"="Diana",
  "age"=25,
  "favFood"="sushi"
}
```

__GET__  
`localhost:5000/form`  
Data will be updated.

__DELETE__
```
{
  "id"=1
}
```

__GET__  
`localhost:5000/form`  
Data is deleted.

## Contributing
1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
