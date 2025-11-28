import { FC, useState, useEffect, MouseEvent, FormEvent } from "react";
import axios from "axios";
import { TablePage } from "../TablePage/TablePage.tsx";
import { ContextMenuDatabase } from "../ContextMenuDatabase/ContextMenuDatabase.tsx";
import {CreateDbForm} from "../CreateDbForm/CreateDbForm.tsx";

interface Database {
  id: string;
  nome: string;
  data_creazione: string;
  ora_creazione: string;
}


const initialContextMenu = {
  show: false,
  x: 0,
  y: 0,
};

export const DatabasePage: FC = () => {
  const [databaseList, setDatabaseList] = useState([]);
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedDb, setSelectedDb] = useState<Database | null>(null);
  const [contextMenu, setContextMenu] = useState(initialContextMenu);
  const [selectedDbContext, setSelectedDbContext] = useState<Database>({
    id: "0",
    nome: "nessun db selezionato",
    data_creazione: "0",
    ora_creazione: "0",
  });
  const [isModalOpen, setModalOpen] = useState(false);
  const [isModalNewDbOpen, setModalNewDbOpen] = useState(false);
  const [dbNewName, setDbNewName] = useState<string>("");

  const handleOpen = (selectedTable: Database) => {
    setIsOpen(true);
    setSelectedDb(selectedTable);
  };

  const handleClose = () => {
    setIsOpen(false);
    setSelectedDb(null);
  };

  const handleContextMenu = (event: MouseEvent, databaseEvent: Database) => {
    event.preventDefault();
    const { pageX, pageY } = event;
    setContextMenu({ show: true, x: pageX, y: pageY });
    setSelectedDbContext(databaseEvent);
  };

  const closeContextMenu = () => setContextMenu(initialContextMenu);

  const closeModalNewDb = () => setModalNewDbOpen(false);

  useEffect(() => {
    // Effettua la tua chiamata API e aggiorna lo stato
    const HandlePrintListDb = async () => {
      try {
        const jwtoken = "Bearer " + "$" + sessionStorage.getItem("jwtToken");
        // Effettua la tua autenticazione qui, ad esempio con una chiamata fetch
        const response = await axios.get(
          "http://localhost:8080/visualdb-api/database/search",
          {
            headers: {
              Authorization: jwtoken, // Sostituisci con il tuo token effettivo
              "Content-Type": "application/json",
            },
          }
        );
        if (response.status) {
          console.error(response.data);
          setDatabaseList(response.data);
          console.log(response);
        } else {
          console.error(JSON.stringify(response, null, 4));
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
    HandlePrintListDb();
  }, []);

  const openModal = () => {
    setModalOpen(true);
  };

  const closeModal = () => {
    console.log("chiudo");
    setModalOpen(false);
    console.log("messo a false");
  };

  const handleSubmitRenameDb = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.post(
        "http://localhost:8080/visualdb-api/database/rename",
        {
          body: {
            id: selectedDbContext.id,
            NewName: dbNewName,
          },
        },
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );

      if (data.status === 200) {
        console.log(JSON.stringify(data, null, 4));
        closeModal();
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
      <section className="h-full bg-white text-white mx-auto max-w-screen-xl px-4 py-32 lg:h-full lg:items-center">
        {isModalOpen && (
          // Includi qui il tuo componente Modal e il form
          <div className="fixed z-20 top-0 left-0 w-full h-full flex items-center justify-center bg-gray-800 bg-opacity-75">
            <div className="bg-gray-950 p-8 rounded-lg">
              <form
                className="max-w-sm mx-auto"
                onSubmit={handleSubmitRenameDb}
              >
                <div className="mb-5">
                  <label
                    htmlFor="table"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    Nuovo nome del database: {selectedDbContext.nome}
                  </label>
                  <input
                    type="table"
                    id="table"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="es. Utenti"
                    required
                    onChange={(e) => setDbNewName(e.target.value)}
                  />
                </div>
                <button
                  type="submit"
                  className="m-2 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Submit
                </button>
                <button
                  onClick={closeModal}
                  className="m-2 text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-red-700 dark:hover:bg-red-800 dark:focus:ring-blue-800"
                >
                  Annulla
                </button>
              </form>
            </div>
          </div>
        )}
        {contextMenu.show && (
          <ContextMenuDatabase
            x={contextMenu.x}
            y={contextMenu.y}
            closeContextMenu={closeContextMenu}
            openModal={openModal}
            clickedDatabase={selectedDbContext}
          />
        )}
        {isOpen ? (
          <TablePage
            isOpen={isOpen}
            selectedDb={selectedDb}
            onClose={handleClose}
          />
        ) : (
            <div className="relative overflow-x-auto sm:rounded-lg">
              <table className="w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400">
                <thead className="text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400">
                <tr>
                  <th scope="col" className="px-6 py-3">
                    Nome
                  </th>
                  <th scope="col" className="px-6 py-3">
                    Data di creazione
                  </th>
                  <th scope="col" className="px-6 py-3">
                    Ora di creazione
                  </th>
                  <th scope="col" className="px-6 py-3"></th>
                </tr>
                </thead>
                <tbody>
                {databaseList.map((item: Database, index) => (
                    <tr
                        key={index}
                        className="odd:bg-white odd:dark:bg-gray-900 even:bg-gray-50 even:dark:bg-gray-800 border-b dark:border-gray-700"
                        onContextMenu={(e) => handleContextMenu(e, item)}
                    >
                      <th
                          scope="row"
                          className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white"
                      >
                        {item.nome}
                      </th>
                      <td className="px-6 py-4">{item.data_creazione}</td>
                      <td className="px-6 py-4">{item.ora_creazione}</td>
                      <td className="px-6 py-4">
                        <button
                            onClick={() => handleOpen(item)}
                            className="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm p-1 text-center"
                        >
                          Open
                        </button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
              <div className="flex justify-end mt-4">
                <button
                    onClick={()=>setModalNewDbOpen(true)}
                    className="text-white bg-gradient-to-r from-teal-400 via-teal-500 to-teal-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-teal-300 dark:focus:ring-teal-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
                >
                  Nuovo Database
                </button>
              </div>
            </div>
        )}
      </section>
      {isModalNewDbOpen && (
          // Includi qui il tuo componente Modal e il form
          <CreateDbForm onClose={closeModalNewDb}/>
      )}
    </>
  );
};
/*
 * <svg fill="#000000" version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 482.428 482.429" xml:space="preserve"><g id="SVGRepo_bgCarrier" stroke-width="0"></g><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g><g id="SVGRepo_iconCarrier"> <g> <g> <path d="M381.163,57.799h-75.094C302.323,25.316,274.686,0,241.214,0c-33.471,0-61.104,25.315-64.85,57.799h-75.098 c-30.39,0-55.111,24.728-55.111,55.117v2.828c0,23.223,14.46,43.1,34.83,51.199v260.369c0,30.39,24.724,55.117,55.112,55.117 h210.236c30.389,0,55.111-24.729,55.111-55.117V166.944c20.369-8.1,34.83-27.977,34.83-51.199v-2.828 C436.274,82.527,411.551,57.799,381.163,57.799z M241.214,26.139c19.037,0,34.927,13.645,38.443,31.66h-76.879 C206.293,39.783,222.184,26.139,241.214,26.139z M375.305,427.312c0,15.978-13,28.979-28.973,28.979H136.096 c-15.973,0-28.973-13.002-28.973-28.979V170.861h268.182V427.312z M410.135,115.744c0,15.978-13,28.979-28.973,28.979H101.266 c-15.973,0-28.973-13.001-28.973-28.979v-2.828c0-15.978,13-28.979,28.973-28.979h279.897c15.973,0,28.973,13.001,28.973,28.979 V115.744z"></path> <path d="M171.144,422.863c7.218,0,13.069-5.853,13.069-13.068V262.641c0-7.216-5.852-13.07-13.069-13.07 c-7.217,0-13.069,5.854-13.069,13.07v147.154C158.074,417.012,163.926,422.863,171.144,422.863z"></path> <path d="M241.214,422.863c7.218,0,13.07-5.853,13.07-13.068V262.641c0-7.216-5.854-13.07-13.07-13.07 c-7.217,0-13.069,5.854-13.069,13.07v147.154C228.145,417.012,233.996,422.863,241.214,422.863z"></path> <path d="M311.284,422.863c7.217,0,13.068-5.853,13.068-13.068V262.641c0-7.216-5.852-13.07-13.068-13.07 c-7.219,0-13.07,5.854-13.07,13.07v147.154C298.213,417.012,304.067,422.863,311.284,422.863z"></path> </g> </g> </g></svg>*/
