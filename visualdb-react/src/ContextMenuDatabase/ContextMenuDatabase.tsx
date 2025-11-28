import { FC } from "react";
import axios from "axios";

interface Database {
  id: string;
  nome: string;
  data_creazione: string;
  ora_creazione: string;
}

interface props {
  x: number;
  y: number;
  closeContextMenu: () => void;
  openModal: () => void;
  clickedDatabase: Database;
}

export const ContextMenuDatabase: FC<props> = ({
  x,
  y,
  closeContextMenu,
  openModal,
  clickedDatabase,
}) => {
  const deleteDatabase = async () => {
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.delete(
        `http://localhost:8080/visualdb-api/database/delete?id=${clickedDatabase.id}`,
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );

      if (data.status === 200) {
        console.log(JSON.stringify(data, null, 4));
        closeContextMenu();
        window.location.reload()
      } else {
        console.error(JSON.stringify(data, null, 4));
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log("error message: ", error.message);
        return error.message;
      } else {
        console.log("unexpected error: ", error);
        return "An unexpected error occurred";
      }
    }
  };

  return (
    <>
      <div
        onClick={() => closeContextMenu()}
        className="absolute z-20"
        style={{ top: `${y}px`, left: `${x}px` }}
      >
        <div
          id="dropdown"
          className="bg-white divide-y divide-gray-100 rounded-lg shadow w-44 dark:bg-gray-700"
        >
          <h1 className="px-6 py-2 font-medium text-white">
            {clickedDatabase.nome}
          </h1>
          <ul
            className="py-2 text-sm text-gray-700 dark:text-gray-200"
            aria-labelledby="dropdownDefaultButton"
          >
            <li>
              <button
                onClick={openModal}
                className="block px-4 py-2 w-full hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white"
              >
                Rinomina
              </button>
            </li>
            <li>
              <button
                onClick={deleteDatabase}
                className="block px-4 py-2 w-full hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white"
              >
                Cancella
              </button>
            </li>
          </ul>
        </div>
      </div>
    </>
  );
};
