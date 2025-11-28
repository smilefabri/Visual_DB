import { useEffect, useState } from "react";
import axios from "axios";
import { FC } from "react";

interface Table {
  id: string;
  nome: string;
  data_creazione: string;
  database: string;
  ora_creazione: string;
}

interface DatiTabella {
  [key: string]: string;
}

interface TabellaDatiProps {
  selectedTable: Table | null;
}

export const TableVisual: FC<TabellaDatiProps> = ({ selectedTable }) => {
  const [dati, setDati] = useState<DatiTabella[]>([]);
  const [showAddColumn, setShowAddColumn] = useState(false);
  const [newColumnName, setNewColumnName] = useState("");
  const [selectedColumnType, setSelectedColumnType] = useState("Tipo");
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (selectedTable) {
        try {
          const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
          // Esegui una richiesta POST al server per ottenere i dati della tabella
          const response = await axios.post(
            "http://localhost:8080/visualdb-api/table/search",
            {
              body: {
                nameTable: selectedTable.nome,
                idDb: selectedTable.database,
                idTable: selectedTable.id,
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
            setDati(response.data);
            console.error(dati);
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
      }
    };
    fetchData();
  }, [selectedTable]);

  const handleAddColumn = async (selectedTable: Table) => {
    try {
      // Verifica che il nome della nuova colonna non sia vuoto
      if (newColumnName.trim() !== "" && selectedColumnType.trim() !== "Tipo") {
        const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
        const response = await axios.post(
          "http://localhost:8080/visualdb-api/table/addcolumn",
          {
            nameTable: selectedTable.nome,
            nameNewColumn: newColumnName.trim(),
            idDb: selectedTable.database,
            idTable: selectedTable.id,
            typeData: selectedColumnType.trim().toUpperCase(),
          },
          {
            headers: {
              Authorization: jwtoken,
              "Content-Type": "application/json",
            },
          }
        );

        // Controlla se la richiesta ha avuto successo
        if (response.data && response.data.success) {
          // Aggiungi la nuova colonna a tutte le righe
          const updatedDati = dati.map((riga) => ({
            ...riga,
            [newColumnName]: " ",
          }));
          setDati(updatedDati);

          // Resetta lo stato per nascondere il prompt
          setShowAddColumn(false);
          setSelectedColumnType("Tipo");
          setNewColumnName("");
        } else {
          console.error(
            "Errore nell'aggiunta della colonna:",
            response.data && response.data.message
          );
        }
      }
    } catch (error) {
      console.error(
        "Errore durante l'aggiunta della colonna:",
        (error as Error).message
      );
    }
  };

  const handleSaveData = async (selectedTable: Table) => {
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      const response = await axios.post(
        "http://localhost:8080/visualdb-api/table/save",
        {
          nameTable: selectedTable.nome,
          idDb: selectedTable.database,
          idTable: selectedTable.id,
          array: dati,
        },
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );
      if (response.data && response.data.success) {
        console.log("Success");
      } else {
        console.error(
          "Errore nell'aggiunta della colonna:",
          response.data && response.data.message
        );
      }
    } catch (error) {
      console.error(
        "Errore durante il salvataggio, (error as Error).message);"
      );
    }
  };

  const handleAddRow = () => {
    const newRow: DatiTabella = {};
    colonne.forEach((colonna) => {
      newRow[colonna] = "";
    });

    // Aggiungi la nuova riga alla tabella
    setDati((prevDati) => [...prevDati, newRow]);
  };

  const handleCellChange = (
    rowIndex: number,
    columnName: string,
    value: string
  ) => {
    setDati((prevDati) => {
      const updatedDati = [...prevDati];
      updatedDati[rowIndex] = {
        ...updatedDati[rowIndex],
        [columnName]: value,
      };
      return updatedDati;
    });
  };

  const handleDeleteRow = (rowIndex: number) => {
    setDati((prevDati) => {
      const updatedDati = [...prevDati];
      updatedDati.splice(rowIndex, 1); // Rimuovi la riga corrispondente all'indice
      return updatedDati;
    });
  };

  const renderAddColumnPrompt = () => {
    return (
      <div className="flex justify-end mb-2 text-gray-500 dark:text-gray-400">
        <div className="relative inline-block text-left overflow-y-visible">
          <input
            className="font-medium text-sm px-5 py-2.5 me-2 mb-2 text-gray-500 rounded-lg dark:text-white bg-gray-100 dark:bg-gray-700 group"
            type="text"
            placeholder="Nome della nuova colonna"
            value={newColumnName}
            onChange={(e) => setNewColumnName(e.target.value)}
          />
          <button
            type="button"
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="text-white bg-gradient-to-r from-cyan-400 via-cyan-500 to-cyan-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-cyan-300 dark:focus:ring-cyan-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
          >
            {selectedColumnType || "Tipo"}
          </button>
          {dropdownOpen && (
            <div className="origin-top-right  absolute right-0 mt-2 w-40 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 focus:outline-none">
              <div className="py-1">
                {[
                  "Integer",
                  "Decimal",
                  "Double Precision",
                  "Text",
                  "Boolean",
                ].map((type) => (
                  <button
                    key={type}
                    onClick={() => {
                      setSelectedColumnType(type);
                      setDropdownOpen(false);
                    }}
                    className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                  >
                    {type}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
        <button
          onClick={() => handleAddColumn(selectedTable as Table)}
          className="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
        >
          Add
        </button>
      </div>
    );
  };

  if (!selectedTable) {
    return (
      <div className="flex-1 ml-1/5 w-full p-4">
        <div className="w-full overflow-x-auto shadow-md sm:rounded-lg mx-auto">
          <table className="w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400">
            <caption className="p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800">
              Nessuna tabella selezionata
            </caption>
          </table>
        </div>
      </div>
    );
  }

  if (!dati || dati.length === 0) {
    return (
      <div className="flex-1 ml-1/5 w-full p-4">
        <div className="w-full sm:rounded-lg mx-auto">
          <div className="flex justify-end mb-2 text-gray-500 dark:text-gray-400">
            {/* Aggiungi il pulsante per mostrare/nascondere il prompt */}
            <button
              onClick={() => setShowAddColumn(!showAddColumn)}
              className="p-2 text-gray-500 rounded-lg dark:text-white bg-gray-100 dark:bg-gray-700 group"
            >
              Nuova Colonna
            </button>
          </div>
          {showAddColumn && renderAddColumnPrompt()}
          <table className="w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400">
            <caption className="p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800">
              Nessun dato disponibile
            </caption>
          </table>
        </div>
      </div>
    );
  }

  // Estrai le colonne dalla prima riga dell'array
  const colonne = Object.keys(dati[0]);

  return (
    <>
      <div className="flex-1 ml-1/5 w-full p-4">
        <div className="w-full h-full sm:rounded-lg mx-auto">
          <div className="flex justify-end mb-2 text-gray-500 dark:text-gray-400">
            {/* Aggiungi il pulsante per mostrare/nascondere il prompt */}
            <button
              onClick={() => setShowAddColumn(!showAddColumn)}
              className="text-white bg-gradient-to-r from-teal-400 via-teal-500 to-teal-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-teal-300 dark:focus:ring-teal-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
            >
              Nuova Colonna
            </button>
          </div>
          {showAddColumn && renderAddColumnPrompt()}
          <table className="w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400">
            <caption className="p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800">
              {selectedTable.nome}
            </caption>
            <thead className="text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400">
              <tr>
                {/* Stampa gli header delle colonne */}
                {colonne.map((colonna) => (
                  <th key={colonna} scope="col" className="px-6 py-3">
                    {colonna}
                  </th>
                ))}
                <th key="del" scope="col" className="px-6 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {/* Itera sulle righe dei dati e stampa le celle */}
              {dati.map((riga, index) => (
                <tr
                  key={riga.id || index + 1}
                  className="bg-white border-b dark:bg-gray-800 dark:border-gray-700"
                >
                  {colonne.map((colonna) => (
                    <td key={colonna} className="px-6 py-4">
                      {colonna != "id" ? (
                        <input
                          className="p-2 mr-2 text-gray-500 rounded-lg dark:text-white bg-gray-100 dark:bg-gray-700 group"
                          type="text"
                          placeholder={riga[colonna]}
                          value={riga[colonna]}
                          onChange={(e) =>
                            handleCellChange(index, colonna, e.target.value)
                          }
                        />
                      ) : (
                        riga[colonna] || index + 1
                      )}
                    </td>
                  ))}
                  {dati.length === 1 ? (
                      <td className="px-6 py-4 cursor-not-allowed">
                        {/* Aggiungi un'icona di eliminazione con un gestore di eventi onClick */}
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            className="w-6 h-6 text-white bg-gradient-to-r from-red-400 via-red-500 to-red-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-red-300 dark:focus:ring-red-800 font-medium rounded-lg text-sm text-center p-1"
                        >
                          <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </td>
                  ) : (
                      <td
                          className="px-6 py-4 cursor-pointer"
                          onClick={() => handleDeleteRow(index)}
                      >
                        {/* Aggiungi un'icona di eliminazione con un gestore di eventi onClick */}
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            className="w-6 h-6 text-white bg-gradient-to-r from-red-400 via-red-500 to-red-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-red-300 dark:focus:ring-red-800 font-medium rounded-lg text-sm text-center p-1"
                        >
                          <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
          <div className="flex justify-end mt-4">
            <button
              onClick={handleAddRow}
              className="text-white bg-gradient-to-r from-teal-400 via-teal-500 to-teal-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-teal-300 dark:focus:ring-teal-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
            >
              Aggiungi Riga
            </button>
            <button
              onClick={() => handleSaveData(selectedTable)}
              className="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
            >
              Salva
            </button>
          </div>
        </div>
      </div>
    </>
  );
};
