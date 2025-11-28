import { FC, FormEvent, useState } from "react";
import logo from "../assets/VisualDB_Logo.png";
import tex from "../assets/texture.jpg";
import axios from "axios";
import { SignInForm } from "../SignInForm/SignInForm.tsx";

interface LoginProps {
  setToken: (userToken: { token: string }) => void;
  setPrivilegeSession: (privilege: number) => void;
  setPrivilege: (privilege: number ) => void;
}

type CreateUserResponse = {
  operation: string;
  privilege: number;
  username: string;
  status: boolean;
  token: string;
  errorMessage: string;
};

export const LoginForm: FC<LoginProps> = ({ setToken, setPrivilegeSession, setPrivilege }) => {
  const [username, setUserName] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [showSignInForm, setShowSignInForm] = useState<boolean>(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Simula una richiesta di login
    try {
      const myHeaders = new Headers();
      myHeaders.append("Content-Type", "application/json");

      const raw = JSON.stringify({
        username: username,
        password: password,
      });
      console.log(raw);
      const requestOptions: RequestInit = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow",
      };
      // Effettua la tua autenticazione qui, ad esempio con una chiamata fetch
      const { data } = await axios.post<CreateUserResponse>(
        "http://localhost:8080/visualdb-api/login",
        requestOptions
      );

      if (data.status) {
        console.log(JSON.stringify(data, null, 4));
        setToken({ token: data.username });
        setPrivilegeSession(data.privilege);
        setPrivilege(data.privilege);
        sessionStorage.setItem("jwtToken", data.token);
        window.location.reload();
      } else {
        console.error(JSON.stringify(data, null, 4));
      }
      //return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log("error message: ", error.message);
        // 👇️ error: AxiosError<any, any>
        return error.message;
      } else {
        console.log("unexpected error: ", error);
        return "An unexpected error occurred";
      }
    }
  };

  const handleRegister = () => {
    console.log("click click");
    setShowSignInForm(true);
  };

  return (
    <>
      {!showSignInForm && (
        <section className="bg-white w-screen h-screen">
          <div className="lg:grid lg:min-h-screen lg:grid-cols-12">
            <aside className="relative block h-16 lg:order-last lg:col-span-5 lg:h-full xl:col-span-6">
              <img
                alt="Pattern"
                src={tex}
                className="absolute inset-0 h-full w-full object-cover"
              />
            </aside>

            <main className="flex items-center justify-center px-8 py-8 sm:px-12 lg:col-span-7 lg:px-16 lg:py-12 xl:col-span-6">
              <div className="max-w-xl lg:max-w-3xl">
                <a className="block text-blue-600" href="/">
                  <span className="sr-only">Home</span>
                  <img className="h-16 sm:h-20" src={logo} alt="..." />
                </a>

                <h1 className="mt-6 text-2xl font-bold text-gray-900 sm:text-3xl md:text-4xl">
                  Welcome to VisualDB 🦑
                </h1>

                <form
                  onSubmit={handleSubmit}
                  className="mt-8 grid grid-cols-6 gap-6"
                >
                  <div className="col-span-12 sm:col-span-3">
                    <label
                      htmlFor="username"
                      className="block text-sm font-medium text-gray-700 h-8"
                    >
                      Username
                    </label>
                    <input
                      type="text"
                      id="username"
                      name="username"
                      value={username}
                      onChange={(e) => setUserName(e.target.value)}
                      className="mt-1 w-full rounded-md border-gray-200 bg-white text-sm text-gray-700 shadow-sm h-8"
                    />
                  </div>

                  <div className="col-span-6 sm:col-span-3">
                    <label
                      htmlFor="password"
                      className="block text-sm font-medium text-gray-700 h-8"
                    >
                      Password
                    </label>
                    <input
                      type="password"
                      id="password"
                      name="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="mt-1 w-full rounded-md border-gray-200 bg-white text-sm text-gray-700 shadow-sm h-8"
                    />
                  </div>

                  <div className="col-span-6 sm:flex sm:items-center sm:gap-4">
                    <button className="inline-block shrink-0 rounded-md border border-b-cyan-700 bg-cyan-700 px-12 py-3 text-sm font-medium text-white transition hover:bg-transparent hover:text-cyan-700 focus:outline-none focus:ring active:text-cyan-700 hover:border-b-cyan-700">
                      Log In
                    </button>

                    <p className="mt-4 text-sm text-gray-500 sm:mt-0">
                      Non hai ancora un account? &nbsp;
                      <a
                        href="#"
                        className="text-gray-700 underline"
                        onClick={handleRegister}
                      >
                        Registrati
                      </a>
                      .
                    </p>
                  </div>
                </form>
              </div>
            </main>
          </div>
        </section>
      )}
      {showSignInForm && <SignInForm setShowSignInForm={setShowSignInForm}/>}
    </>
  );
};
