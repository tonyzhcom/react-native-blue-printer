//@flow
import { NativeModules } from "react-native";
const {BluePrinter} = NativeModules;

//
// connect = ()=>{
//   BluePrinter.connect( (msg)=>console.log("connect cb",{msg}))
// }
// print = (res)=>{
//   console.log(res.split("file://")[1]);
//   BluePrinter.connect( (msg)=>BluePrinter.print(res.split("file://")[1] ,(msg)=>console.log("print cb",{msg})))
// }

export default BluePrinter 
