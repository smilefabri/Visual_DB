import { FC, useState } from "react";
import axios from "axios";

interface ModalProps {
  onClose: () => void;
}

export const CreateDbForm: FC<ModalProps> = ({ onClose }) => {
  const [NewNameDb, setNewNameDb] = useState<string>("");

  const handleSubmitCreateDb = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.post(
        "http://localhost:8080/visualdb-api/database/new",
        {
          body: {
            NewName: NewNameDb,
          },
        },
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );
      // Effettua la tua autenticazione qui, ad esempio con una chiamata fetch
      if (data.status) {
        console.log(JSON.stringify(data, null, 4));
        window.location.reload();
      } else {
        console.error(JSON.stringify(data, null, 4));
      }
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
  return (
    <>
      <div className="fixed z-20 top-0 left-0 w-full h-full flex items-center justify-center bg-gray-800 bg-opacity-75">
        <div className="bg-gray-950 p-8 rounded-lg">
          <form className="max-w-sm mx-auto" onSubmit={handleSubmitCreateDb}>
            <div className="mb-5">
              <label
                  htmlFor="databse"
                  className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Nome del database
              </label>
              <input
                  type="text"
                  id="databse"
                  className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                  placeholder="es. Utenti"
                  required
                  onChange={(e) => setNewNameDb(e.target.value)}
              />
            </div>
            <button
                type="submit"
                className="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
            >
              Submit
            </button>
            <button
                onClick={onClose}
                className="text-white bg-gradient-to-r from-red-400 via-red-500 to-red-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-red-300 dark:focus:ring-red-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
            >
              Annulla
            </button>
          </form>
        </div>
      </div>
    </>
  );
};
