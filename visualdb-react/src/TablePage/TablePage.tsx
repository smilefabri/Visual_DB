import {FC, FormEvent, MouseEvent, useEffect, useState} from "react";
import axios from "axios";
import { TableVisual } from "../TableVisual/TableVisual.tsx";
import { ModalAddTable } from "../ModalAddTable/ModalAddTable.tsx";
import { ContextMenuTable } from "../ContextMenuTable/ContextMenuTable.tsx";
import "./TablePage.css";

interface Database {
  id: string;
  nome: string;
  data_creazione: string;
  ora_creazione: string;
}

interface ModalProps {
  isOpen: boolean;
  selectedDb: Database | null;
  onClose: () => void;
}

interface Table {
  id: string;
  nome: string;
  data_creazione: string;
  database: string;
  ora_creazione: string;
}

const initialContextMenu = {
  show: false,
  x: 0,
  y: 0,
};

export const TablePage: FC<ModalProps> = ({ isOpen, selectedDb, onClose }) => {
  const [listOfTables, setListOfTables] = useState<Table[]>([]);
  const [selectedTable, setSelectedTable] = useState<Table | null>(null);
  const [isSidebarVisible, setIsSidebarVisible] = useState(true); // Aggiunto stato per la visibilità della Sidebar
  const [isModalOpen, setModalOpen] = useState(false);
  const [contextMenu, setContextMenu] = useState(initialContextMenu);
  const [selectedTableContext, setSelectedTableContext] = useState<Table>({
    id: "0",
    nome: "nessun db selezionato",
    data_creazione: "0",
    database: "0",
    ora_creazione: "0",
  });
  const [isModalOpenRename, setModalOpenRename] = useState(false);
  const [tableNewName, setTableNewName] = useState<string>("");

  //const [, updateState] = useState();
  //const forceUpdate = useCallback(() => updateState({}), []);

  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);

  const openModalRename = () => setModalOpenRename(true);
  const closeModalRename = () => setModalOpenRename(false);

  const handleTable = (item: Table) => {
    console.error(item);
    setSelectedTable(item);
  };

  const handleToHome = () => {
    setSelectedTable(null);
  };

  const toggleSidebar = () => {
    setIsSidebarVisible(!isSidebarVisible);
  };

  if (!isOpen || !selectedDb) {
    return null;
  }

  // eslint-disable-next-line react-hooks/rules-of-hooks
  useEffect(() => {
    const handleListTables = async () => {
      //e.preventDefault();
      try {
        const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
        console.debug(jwtoken);
        const response = await axios.post(
          "http://localhost:8080/visualdb-api/database/returntable",
          {
            body: {
              idDb: selectedDb.id,
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
        if (response.status) {
          console.error(response.data);
          setListOfTables(response.data);
          console.log(response);
        } else {
          console.error(JSON.stringify(response, null, 4));
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
    handleListTables();
  }, []);

  const handleContextMenu = (e: MouseEvent, item: Table) => {
    e.preventDefault();
    const { pageX, pageY } = e;
    setContextMenu({ show: true, x: pageX, y: pageY });
    setSelectedTableContext(item);
  };

  const closeContextMenu = () => setContextMenu(initialContextMenu);

  const handleSubmitRenameDb = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    console.log("handleSubmitRenameDb: " + tableNewName);
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.post(
        "http://localhost:8080/visualdb-api/table/rename",
        {
          body: {
            idTable: selectedTableContext.id,
            nameTable: selectedTableContext.nome,
            idDatabase: selectedTableContext.database,
            newNameTable: tableNewName,
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
        const updatedList = listOfTables.map(table =>
            table.id === selectedTableContext.id ? { ...table, nome: tableNewName } : table
        );

        // Aggiorna lo stato con la nuova lista
        setListOfTables(updatedList);
        closeModalRename();
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
      <div className="flex">
        {isSidebarVisible && (
          <div
            onClick={toggleSidebar} // Chiudi la Sidebar cliccando sull'overlay
            style={{
              position: "fixed",
              top: 0,
              left: 0,
              width: "100%",
              height: "100%",
              zIndex: 0, // Assegna un valore inferiore rispetto alla Sidebar
              display: isSidebarVisible ? "block" : "none",
            }}
            className="bg-gray-800 bg-opacity-75"
          ></div>
        )}
        {isModalOpenRename && (
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
                    Nuovo nome della tabella: {selectedTableContext.nome}
                  </label>
                  <label
                      htmlFor="table"
                      className="block mb-2 text-sm font-light text-gray-600 dark:text-gray-600"
                  >
                    * gli spazi vuoti verranno sostituiti con degli underscore
                  </label>
                  <input
                      type="table"
                      id="table"
                      className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                      placeholder="es. Utenti"
                      required
                      onChange={(e) => setTableNewName(e.target.value)}
                  />
                </div>
                <button
                    type="submit"
                    className="m-2 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Submit
                </button>
                <button
                  onClick={closeModalRename}
                  className="m-2 text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-red-700 dark:hover:bg-red-800 dark:focus:ring-blue-800"
                >
                  Annulla
                </button>
              </form>
            </div>
          </div>
        )}
        {contextMenu.show && (
          <ContextMenuTable
            x={contextMenu.x}
            y={contextMenu.y}
            closeContextMenu={closeContextMenu}
            openModal={openModalRename}
            clickedTable={selectedTableContext}
            setClickedTable={setSelectedTableContext}
            listOfTables={listOfTables}
            setListOfTables={setListOfTables}
            setSelectedTable={setSelectedTable}
          />
        )}
        {isSidebarVisible && ( // Mostra la Sidebar solo se isSidebarVisible è true
          <aside
            id="default-sidebar"
            className="fixed left-0 z-10 w-1/5 transition-transform -translate-x-full sm:translate-x-0 bg-gray-950 rounded"
          >
            <div className="h-full px-3 py-4 overflow-y-auto">
              <ul className="space-y-2 font-medium">
                <li>
                  <a
                    href="#"
                    onClick={() => handleToHome()}
                    className="flex items-center p-2 text-gray-500 rounded-lg dark:text-white bg-gray-100 dark:bg-gray-700 group"
                  >
                    <svg
                      className="w-5 h-5 text-gray-500 transition duration-75 dark:text-white hover:text-white"
                      aria-hidden="true"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="currentColor"
                      viewBox="0 0 22 21"
                    >
                      <path d="M16.975 11H10V4.025a1 1 0 0 0-1.066-.998 8.5 8.5 0 1 0 9.039 9.039.999.999 0 0 0-1-1.066h.002Z" />
                      <path d="M12.5 0c-.157 0-.311.01-.565.027A1 1 0 0 0 11 1.02V10h8.975a1 1 0 0 0 1-.935c.013-.188.028-.374.028-.565A8.51 8.51 0 0 0 12.5 0Z" />
                    </svg>
                    <span className="ms-3">{selectedDb.nome}</span>
                  </a>
                </li>
                <hr className="style"/>
                {listOfTables.map((item: Table) =>
                  selectedTable?.id == item.id ? (
                    <li
                      key={item.id}
                      onContextMenu={(e) => handleContextMenu(e, item)}
                    >
                      <a
                        href="#"
                        onClick={() => handleTable(item)}
                        className="flex items-center p-2 text-gray-500 rounded-lg dark:text-white bg-blue-700 dark:bg-blue-700 group"
                      >
                        <svg
                          className="w-5 h-5 text-gray-500 transition duration-75 dark:text-white hover:text-white"
                          aria-hidden="true"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="currentColor"
                          viewBox="0 0 18 18"
                        >
                          <path d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10Zm10 0h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286A1.857 1.857 0 0 0 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z" />
                        </svg>
                        <span className="flex-1 ms-3 whitespace-nowrap">
                          {item.nome}
                        </span>
                      </a>
                    </li>
                  ) : (
                    <li key={item.id}>
                      <a
                        href="#"
                        onClick={() => handleTable(item)}
                        className="flex items-center p-2 text-gray-500 rounded-lg dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                      >
                        <svg
                          className="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
                          aria-hidden="true"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="currentColor"
                          viewBox="0 0 18 18"
                        >
                          <path d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10Zm10 0h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286A1.857 1.857 0 0 0 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z" />
                        </svg>
                        <span className="flex-1 ms-3 whitespace-nowrap">
                          {item.nome}
                        </span>
                      </a>
                    </li>
                  )
                )}
                <hr className="style"/>
                <li>
                  <a
                    href="#"
                    onClick={openModal}
                    className="flex items-center p-2 text-gray-500 rounded-lg dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                  >
                    <svg
                      className="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M12 6v12M6 12h12"
                      ></path>
                    </svg>
                    <span className="flex-1 ms-3 whitespace-nowrap">
                      Nuova Tabella
                    </span>
                  </a>
                </li>
                <li>
                  <a
                    href="#"
                    onClick={onClose}
                    className="flex items-center p-2 text-gray-500 rounded-lg dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
                  >
                    <svg
                      className="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
                      aria-hidden="true"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 18 16"
                    >
                      <path
                        stroke="currentColor"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M1 8h11m0 0L8 4m4 4-4 4m4-11h3a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-3"
                      />
                    </svg>
                    <span className="flex-1 ms-3 whitespace-nowrap">
                      I tuoi database
                    </span>
                  </a>
                </li>
              </ul>
            </div>
          </aside>
        )}
        <div
          className={`flex-1 ml-${isSidebarVisible ? "1/5" : "0"} w-full p-4`}
        >
          {/* Aggiunto bottone per mostrare/nascondere la Sidebar */}
          <button
            className="absolute bottom-2 left-2 p-2 text-gray-500 rounded-lg dark:text-white bg-gray-100 dark:bg-gray-700 group"
            onClick={toggleSidebar}
          >
            {isSidebarVisible ? (
              // Icona per la chiusura della sidebar
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            ) : (
              // Icona per l'apertura della sidebar
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16m-7 6h7"
                ></path>
              </svg>
            )}
          </button>

          {isModalOpen && (
            // Includi qui il tuo componente Modal e il form
            <ModalAddTable onClose={closeModal} database={selectedDb.id} listOfTables={listOfTables} setListOfTables={setListOfTables}/>
          )}

          <div className="w-full h-full sm:rounded-lg mx-auto">
            <TableVisual selectedTable={selectedTable}/>
          </div>
        </div>
      </div>
    </>
  );
};
