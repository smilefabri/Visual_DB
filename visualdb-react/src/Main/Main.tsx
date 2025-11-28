import "./Main.css";
import {FC, useState} from "react";
import {Navbar} from "../Navbar/Navbar.tsx";
import {LoginForm} from "../LoginForm/LoginForm.tsx";
import {DatabasePage} from "../DatabasePage/DatabasePage.tsx";
import {AdminPage} from "../AdminPage/AdminPage.tsx";

interface UserToken {
    token: string | undefined;
}

function setToken(userToken: UserToken) {
    sessionStorage.setItem("token", JSON.stringify(userToken));
}

function setPrivilegeSession(privilege: number) {
    sessionStorage.setItem("privilege", privilege.toString());
}

function getToken(): string | undefined {
    const tokenString = sessionStorage.getItem("token");
    const userToken: UserToken | null = JSON.parse(tokenString || "null");
    return userToken?.token;
}

interface ParentProps {
    setToken: (userToken: UserToken) => void;
    username: string;
}

export const Main: FC = () => {
    const token = getToken();
    const storedPrivilege = sessionStorage.getItem("privilege");
    const storedPrivilegeNumber = storedPrivilege ? parseInt(storedPrivilege, 10) : -1;
    const [privilege, setPrivilege] = useState<number>(storedPrivilegeNumber);

    //console.log(token)
    if (!token) {
        return <LoginForm setToken={setToken} setPrivilegeSession={setPrivilegeSession} setPrivilege={setPrivilege}/>;
    }
    //console.log(token)
    const props: ParentProps = {setToken, username: token || ""};

    console.log(privilege)

    if (privilege == 1) {
        return (
            <>
                <Navbar {...props} />
                <AdminPage/>
            </>
        )
    } else if (privilege == 0) {
        return (
            <>
                <Navbar {...props} />
                <DatabasePage/>
            </>
        );
    }
};
